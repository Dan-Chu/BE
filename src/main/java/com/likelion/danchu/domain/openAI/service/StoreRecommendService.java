package com.likelion.danchu.domain.openAI.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.danchu.domain.hashtag.dto.response.HashtagResponse;
import com.likelion.danchu.domain.hashtag.entity.Hashtag;
import com.likelion.danchu.domain.hashtag.mapper.HashtagMapper;
import com.likelion.danchu.domain.mission.entity.Mission;
import com.likelion.danchu.domain.mission.repository.MissionRepository;
import com.likelion.danchu.domain.openAI.dto.response.StoreRecommendResponse;
import com.likelion.danchu.domain.openAI.exception.OpenAIErrorCode;
import com.likelion.danchu.domain.store.entity.Store;
import com.likelion.danchu.domain.store.entity.StoreHashtag;
import com.likelion.danchu.domain.store.repository.StoreHashtagRepository;
import com.likelion.danchu.domain.store.repository.StoreRepository;
import com.likelion.danchu.domain.user.entity.User;
import com.likelion.danchu.domain.user.entity.UserHashtag;
import com.likelion.danchu.domain.user.exception.UserErrorCode;
import com.likelion.danchu.domain.user.repository.UserHashtagRepository;
import com.likelion.danchu.domain.user.repository.UserRepository;
import com.likelion.danchu.global.exception.CustomException;
import com.likelion.danchu.global.security.SecurityUtil;
import com.likelion.danchu.infra.openAI.OpenAIUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreRecommendService {

  private final OpenAIUtil openAIUtil;
  private final UserRepository userRepository;
  private final UserHashtagRepository userHashtagRepository;
  private final MissionRepository missionRepository;
  private final StoreRepository storeRepository;
  private final StoreHashtagRepository storeHashtagRepository;
  private final HashtagMapper hashtagMapper;

  private static final int TOP_K = 5;

  /**
   * 현재 로그인한 사용자의 태그/미션 이력을 바탕으로 상위 5개 가게를 추천합니다.
   *
   * <p>알고리즘 단계:
   *
   * <ol>
   *   <li><b>1차 후보 선정(내부 로직)</b>: 해시태그 교집합 개수(k1) 우선 → 미션 참여 수(k2) → 가게 ID 내림차순으로 정렬해 최대 5개 선별.
   *   <li><b>임베딩 재정렬</b>: 사용자 태그 텍스트 vs 후보(설명+태그) 텍스트를 임베딩해 코사인 유사도로 재정렬. 임베딩 호출 실패 시 1차 선정 순서를 그대로
   *       사용.
   *   <li><b>해시태그 배치 조회</b>: N+1 방지를 위해 가게별 해시태그를 일괄 조회하여 DTO에 매핑.
   * </ol>
   *
   * @return 정렬된 최대 5개의 {@link StoreRecommendResponse} 리스트(순서 보장)
   * @throws CustomException 사용자가 없거나(USER_NOT_FOUND), 사용자 해시태그가 비어 있는 경우(USER_HASHTAG_EMPTY)
   */
  public List<StoreRecommendResponse> recommendTop5StoresForCurrentUser() {
    Long userId =
        Optional.ofNullable(SecurityUtil.getCurrentUserId())
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 1) 사용자 해시태그
    List<String> userTags =
        userHashtagRepository.findByUser_Id(userId).stream()
            .map(UserHashtag::getHashtag)
            .map(Hashtag::getName)
            .distinct()
            .toList();
    if (userTags.isEmpty()) {
      throw new CustomException(OpenAIErrorCode.USER_HASHTAG_EMPTY);
    }

    // 2) 사용자 미션 참여 가게 카운트 (storeId -> 참여횟수)
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    Map<Long, Long> missionStoreCount = buildMissionStoreCount(user.getCompletedMissionIds());

    // 3) 모든 가게 태그 맵 (storeId -> tagName 리스트)
    List<StoreHashtag> allStoreTags = storeHashtagRepository.findAllWithStoreAndHashtag();
    Map<Long, List<String>> storeTagMap =
        allStoreTags.stream()
            .collect(
                Collectors.groupingBy(
                    sht -> sht.getStore().getId(),
                    Collectors.mapping(sht -> sht.getHashtag().getName(), Collectors.toList())));

    // 4) 내부 로직으로 1차 후보 5개 뽑기
    List<Long> candidateIds = pickTop5Candidates(userTags, storeTagMap, missionStoreCount);
    if (candidateIds.isEmpty()) return List.of();

    // 5) (옵션) 임베딩 유사도로 5개 재정렬 — 실패시 기존 순서 유지
    List<Store> candidates = storeRepository.findAllById(candidateIds);
    Map<Long, Store> storeMap = candidates.stream().collect(Collectors.toMap(Store::getId, s -> s));

    String userText = normalizeTags(userTags);
    List<String> storeTexts = new ArrayList<>();
    for (Long id : candidateIds) {
      Store s = storeMap.get(id);
      List<String> tags = storeTagMap.getOrDefault(id, List.of());
      storeTexts.add(buildStoreText(s, tags));
    }

    List<Long> finalOrder = candidateIds;
    try {
      List<String> inputs = new ArrayList<>(1 + storeTexts.size());
      inputs.add(userText);
      inputs.addAll(storeTexts);

      var vecs = openAIUtil.embedAll(inputs); // [0]은 user, 이후는 store
      if (vecs.size() == inputs.size()) {
        double[] u = vecs.get(0);
        record Rank(Long id, double sim) {}
        List<Rank> ranks = new ArrayList<>();
        for (int i = 0; i < candidateIds.size(); i++) {
          double sim = OpenAIUtil.cosine(u, vecs.get(i + 1));
          ranks.add(new Rank(candidateIds.get(i), sim));
        }
        ranks.sort((a, b) -> Double.compare(b.sim, a.sim));
        finalOrder = ranks.stream().map(Rank::id).toList();
      } else {
        log.warn("[Embeddings] size mismatch: expected={}, got={}", inputs.size(), vecs.size());
      }
    } catch (Exception e) {
      log.warn("[Embeddings] fallback to deterministic order. cause={}", e.toString());
    }

    // 6) 해시태그를 배치 조회하여 DTO에 매핑(N+1 방지)
    List<StoreHashtag> tagRows = storeHashtagRepository.findByStore_IdIn(finalOrder);
    Map<Long, List<Hashtag>> hashtagEntitiesByStore =
        tagRows.stream()
            .collect(
                Collectors.groupingBy(
                    sh -> sh.getStore().getId(),
                    Collectors.mapping(StoreHashtag::getHashtag, Collectors.toList())));

    List<StoreRecommendResponse> out = new ArrayList<>(finalOrder.size());
    for (Long id : finalOrder) {
      Store s = storeMap.get(id);
      if (s == null) continue;

      List<Hashtag> tagEntities = hashtagEntitiesByStore.getOrDefault(id, List.of());
      List<HashtagResponse> mapped =
          tagEntities.stream()
              .map(hashtagMapper::toResponse)
              .collect(
                  Collectors.collectingAndThen(
                      // 해시태그 ID 기준 중복 제거 + 입력 순서 유지
                      Collectors.toMap(
                          HashtagResponse::getId, h -> h, (a, b) -> a, LinkedHashMap::new),
                      m -> new ArrayList<>(m.values())));

      out.add(
          StoreRecommendResponse.builder()
              .id(s.getId())
              .name(s.getName())
              .mainImageUrl(s.getMainImageUrl())
              .hashtags(mapped)
              .build());
    }

    return out;
  }

  /**
   * 사용자가 완료한 미션 목록을 바탕으로 가게별 미션 참여 횟수를 집계합니다.
   *
   * @param completedMissionIds 사용자 완료 미션 ID 목록 (null 또는 빈 리스트 가능)
   * @return 가게 ID → 미션 참여 횟수 맵(없으면 빈 맵)
   */
  private Map<Long, Long> buildMissionStoreCount(List<Long> completedMissionIds) {
    if (completedMissionIds == null || completedMissionIds.isEmpty()) return Map.of();
    List<Mission> missions = missionRepository.findAllById(completedMissionIds);
    return missions.stream()
        .collect(Collectors.groupingBy(m -> m.getStore().getId(), Collectors.counting()));
  }

  /**
   * 내부 규칙에 따라 상위 5개의 가게 ID를 선별합니다.
   *
   * <p>정렬 키: (k1, k2, id) 내림차순
   *
   * <ul>
   *   <li>k1: 사용자 태그와 가게 태그의 교집합 개수
   *   <li>k2: 해당 가게에 대한 사용자의 미션 참여 횟수
   * </ul>
   *
   * 선별 순서:
   *
   * <ol>
   *   <li>k1 &gt; 0인 가게들 중 위 규칙으로 최대 5개
   *   <li>부족 시 k1==0 &amp;&amp; k2&gt;0 인 가게 추가
   *   <li>그래도 부족하면 id 내림차순으로 채움
   * </ol>
   *
   * @param userTags 사용자 해시태그 목록(중복 제거 권장)
   * @param storeTagMap 가게 ID → 가게 해시태그 명 리스트
   * @param missionStoreCount 가게 ID → 사용자 미션 참여 횟수
   * @return 조건에 맞는 가게 ID 최대 5개(정렬된 상태)
   */
  private List<Long> pickTop5Candidates(
      List<String> userTags,
      Map<Long, List<String>> storeTagMap,
      Map<Long, Long> missionStoreCount) {

    Set<String> u = new HashSet<>(userTags);
    record Sc(long id, int k1, long k2) {}
    List<Sc> all =
        storeTagMap.entrySet().stream()
            .map(
                e -> {
                  int k1 = (int) e.getValue().stream().filter(u::contains).count();
                  long k2 = missionStoreCount.getOrDefault(e.getKey(), 0L);
                  return new Sc(e.getKey(), k1, k2);
                })
            .toList();

    // 1) k1>0만 먼저 뽑기
    List<Long> out =
        all.stream()
            .filter(sc -> sc.k1 > 0)
            .sorted(
                (a, b) -> {
                  int c = Integer.compare(b.k1, a.k1);
                  if (c != 0) return c;
                  c = Long.compare(b.k2, a.k2);
                  if (c != 0) return c;
                  return Long.compare(b.id, a.id);
                })
            .limit(TOP_K)
            .map(sc -> sc.id)
            .collect(Collectors.toCollection(ArrayList::new));

    // 2) 부족하면 k1==0, k2>0(미션 참여)로 채우기
    if (out.size() < TOP_K) {
      all.stream()
          .filter(sc -> sc.k1 == 0 && sc.k2 > 0 && !out.contains(sc.id))
          .sorted(
              (a, b) -> {
                int c = Long.compare(b.k2, a.k2);
                if (c != 0) return c;
                return Long.compare(b.id, a.id);
              })
          .limit(TOP_K - out.size())
          .map(sc -> sc.id)
          .forEach(out::add);
    }

    // 3) 그래도 부족하면 id 내림차순으로 채우기(완전 보강)
    if (out.size() < TOP_K) {
      all.stream()
          .filter(sc -> !out.contains(sc.id))
          .sorted((a, b) -> Long.compare(b.id, a.id))
          .limit(TOP_K - out.size())
          .map(sc -> sc.id)
          .forEach(out::add);
    }
    return out;
  }

  /** "#태그"들을 공백으로 연결한 사용자 텍스트 */
  private String normalizeTags(List<String> tags) {
    return tags.stream()
        .map(t -> t.startsWith("#") ? t.substring(1) : t)
        .collect(Collectors.joining(" "));
  }

  /** 가게 설명 + 태그 문자열 */
  private String buildStoreText(Store s, List<String> tags) {
    String desc = (s != null && s.getDescription() != null) ? s.getDescription() : "";
    String tagStr = normalizeTags(tags);
    return (desc + " " + tagStr).trim();
  }
}
