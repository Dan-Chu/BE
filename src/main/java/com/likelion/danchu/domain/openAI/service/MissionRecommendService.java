package com.likelion.danchu.domain.openAI.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.danchu.domain.hashtag.entity.Hashtag;
import com.likelion.danchu.domain.mission.entity.Mission;
import com.likelion.danchu.domain.mission.exception.MissionErrorCode;
import com.likelion.danchu.domain.mission.repository.MissionRepository;
import com.likelion.danchu.domain.openAI.dto.response.MissionRecommendResponse;
import com.likelion.danchu.domain.openAI.exception.OpenAIErrorCode;
import com.likelion.danchu.domain.store.entity.Store;
import com.likelion.danchu.domain.store.repository.StoreHashtagRepository;
import com.likelion.danchu.domain.user.entity.UserHashtag;
import com.likelion.danchu.domain.user.exception.UserErrorCode;
import com.likelion.danchu.domain.user.repository.UserHashtagRepository;
import com.likelion.danchu.domain.user.repository.UserRepository;
import com.likelion.danchu.global.exception.CustomException;
import com.likelion.danchu.infra.openAI.OpenAIUtil;
import com.likelion.danchu.global.security.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 맞춤형 미션 추천 서비스 (단일 추천).
 *
 * <p>알고리즘:
 *
 * <ol>
 *   <li>k1: 사용자 해시태그 ∩ 가게 해시태그 개수
 *   <li>k2: 미션 완료(인기) 추정치(없으면 0)
 *   <li>정렬키: (k1 desc, k2 desc, missionId desc) 로 1차 후보 상위 5개
 *   <li>후보 5개에 대해 임베딩 코사인 유사도 재랭킹(실패 시 1차 순서 유지)
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionRecommendService {

  private static final int TOP_K = 5;

  private final OpenAIUtil openAIUtil;
  private final UserRepository userRepository;
  private final UserHashtagRepository userHashtagRepository;
  private final MissionRepository missionRepository;
  private final StoreHashtagRepository storeHashtagRepository;

  /** 로그인 사용자에게 미션 1개를 추천 */
  public MissionRecommendResponse recommendTopMissionForCurrentUser() {
    Long userId =
        Optional.ofNullable(SecurityUtil.getCurrentUserId())
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 1) 사용자 관심 해시태그
    List<String> userTags =
        userHashtagRepository.findByUser_Id(userId).stream()
            .map(UserHashtag::getHashtag)
            .map(Hashtag::getName)
            .distinct()
            .toList();
    if (userTags.isEmpty()) {
      throw new CustomException(OpenAIErrorCode.USER_HASHTAG_EMPTY);
    }

    // 2) 미션 + 스토어(fetch-join)
    List<Mission> allMissions = missionRepository.findAllWithStore();
    if (allMissions.isEmpty()) {
      throw new CustomException(MissionErrorCode.MISSION_NOT_FOUND);
    }

    // 2-1) 스토어 해시태그 일괄 조회
    List<Long> storeIds = allMissions.stream().map(m -> m.getStore().getId()).distinct().toList();

    Map<Long, List<String>> storeTagMap =
        storeHashtagRepository.findByStore_IdIn(storeIds).stream()
            .collect(
                Collectors.groupingBy(
                    sh -> sh.getStore().getId(),
                    Collectors.mapping(sh -> sh.getHashtag().getName(), Collectors.toList())));

    // 2-2) 미션별 완료 수(k2) 일괄 집계
    List<Long> missionIds = allMissions.stream().map(Mission::getId).toList();
    Map<Long, Long> k2Map =
        missionRepository.findCompleteCountsByMissionIds(missionIds).stream()
            .collect(
                Collectors.toMap(
                    r -> ((Number) r[0]).longValue(), // missionId
                    r -> ((Number) r[1]).longValue() // completed count
                    ));

    // 3) 스코어링(k1: 태그 교집합 수, k2: 완료 수)
    Set<String> u = new HashSet<>(userTags);
    record Sc(Mission m, int k1, long k2) {}

    List<Sc> scored =
        allMissions.stream()
            .map(
                m -> {
                  List<String> tags = storeTagMap.getOrDefault(m.getStore().getId(), List.of());
                  int k1 = (int) tags.stream().filter(u::contains).count();
                  long k2 = k2Map.getOrDefault(m.getId(), 0L);
                  return new Sc(m, k1, k2);
                })
            .toList();

    // 4) 1차 후보 상위 TOP_K
    List<Mission> candidates =
        scored.stream()
            .sorted(
                (a, b) -> {
                  int c = Integer.compare(b.k1, a.k1);
                  if (c != 0) {
                    return c;
                  }
                  c = Long.compare(b.k2, a.k2);
                  if (c != 0) {
                    return c;
                  }
                  return Long.compare(b.m().getId(), a.m().getId());
                })
            .limit(TOP_K)
            .map(Sc::m)
            .toList();

    if (candidates.isEmpty()) {
      throw new CustomException(MissionErrorCode.MISSION_NOT_FOUND);
    }

    // 5) 임베딩 재랭킹(user text vs mission text)
    String userText = normalizeTags(userTags);
    List<String> missionTexts =
        candidates.stream()
            .map(
                m -> buildMissionText(m, storeTagMap.getOrDefault(m.getStore().getId(), List.of())))
            .toList();

    List<Mission> finalOrder = candidates;
    try {
      List<String> inputs = new ArrayList<>(1 + missionTexts.size());
      inputs.add(userText);
      inputs.addAll(missionTexts);

      var vecs = openAIUtil.embedAll(inputs); // [0] user, 이후 후보 미션들
      if (vecs.size() == inputs.size()) {
        double[] uvec = vecs.get(0);
        record Rank(Mission m, double sim) {}

        List<Rank> ranks = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
          double sim = OpenAIUtil.cosine(uvec, vecs.get(i + 1));
          ranks.add(new Rank(candidates.get(i), sim));
        }
        ranks.sort((x, y) -> Double.compare(y.sim, x.sim));
        finalOrder = ranks.stream().map(Rank::m).toList();
      } else {
        log.warn("[Embeddings] size mismatch: expected={}, got={}", inputs.size(), vecs.size());
      }
    } catch (Exception e) {
      log.warn("[Embeddings] fallback to deterministic order. cause={}", e.toString());
    }

    // 6) 최종 1개만 DTO로 반환
    Mission top = finalOrder.get(0);
    return toDto(top);
  }

  /** "#태그"들을 공백으로 연결한 텍스트 */
  private String normalizeTags(List<String> tags) {
    return tags.stream()
        .map(t -> t.startsWith("#") ? t.substring(1) : t)
        .collect(Collectors.joining(" "));
  }

  /** 미션 텍스트(제목 + 설명 + 가게 태그) */
  private String buildMissionText(Mission m, List<String> storeTags) {
    String title = Optional.ofNullable(m.getTitle()).orElse("");
    String desc = Optional.ofNullable(m.getDescription()).orElse("");
    String tagStr = normalizeTags(storeTags);
    return (title + " " + desc + " " + tagStr).trim();
  }

  private MissionRecommendResponse toDto(Mission m) {
    Store s = m.getStore();
    return MissionRecommendResponse.builder()
        .missionId(m.getId())
        .title(m.getTitle())
        .rewardName(m.getReward())
        .storeName(s != null ? s.getName() : null)
        .build();
  }
}
