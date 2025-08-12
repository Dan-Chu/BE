package com.likelion.danchu.domain.openAI.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.danchu.domain.hashtag.entity.Hashtag;
import com.likelion.danchu.domain.mission.entity.Mission;
import com.likelion.danchu.domain.mission.repository.MissionRepository;
import com.likelion.danchu.domain.openAI.exception.OpenAIErrorCode;
import com.likelion.danchu.domain.store.dto.response.StoreResponse;
import com.likelion.danchu.domain.store.entity.Store;
import com.likelion.danchu.domain.store.entity.StoreHashtag;
import com.likelion.danchu.domain.store.mapper.StoreMapper;
import com.likelion.danchu.domain.store.repository.StoreHashtagRepository;
import com.likelion.danchu.domain.store.repository.StoreRepository;
import com.likelion.danchu.domain.user.entity.User;
import com.likelion.danchu.domain.user.entity.UserHashtag;
import com.likelion.danchu.domain.user.exception.UserErrorCode;
import com.likelion.danchu.domain.user.repository.UserHashtagRepository;
import com.likelion.danchu.domain.user.repository.UserRepository;
import com.likelion.danchu.global.exception.CustomException;
import com.likelion.danchu.global.openAI.OpenAIUtil;
import com.likelion.danchu.global.security.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OpenAIService {

  private final OpenAIUtil openAIUtil; // 임베딩 전용
  private final UserRepository userRepository;
  private final UserHashtagRepository userHashtagRepository;
  private final MissionRepository missionRepository;
  private final StoreRepository storeRepository;
  private final StoreHashtagRepository storeHashtagRepository;
  private final StoreMapper storeMapper;

  private static final int TOP_K = 5;

  public List<StoreResponse> recommendTop3StoresForCurrentUser() {
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
    if (userTags.isEmpty()) throw new CustomException(OpenAIErrorCode.USER_HASHTAG_EMPTY);

    // 2) 사용자 완료 미션 → 가게별 참여 횟수
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    Map<Long, Long> missionStoreCount = buildMissionStoreCount(user.getCompletedMissionIds());

    // 3) 모든 가게별 태그
    List<StoreHashtag> allStoreTags = storeHashtagRepository.findAllWithStoreAndHashtag();
    Map<Long, List<String>> storeTagMap =
        allStoreTags.stream()
            .collect(
                Collectors.groupingBy(
                    sht -> sht.getStore().getId(),
                    Collectors.mapping(sht -> sht.getHashtag().getName(), Collectors.toList())));

    // 4) 내부 로직으로 1차 후보 5개 선발
    List<Long> candidateIds = pickTop5Candidates(userTags, storeTagMap, missionStoreCount);

    if (candidateIds.isEmpty()) return List.of();

    // 5) 후보 5개의 설명/태그 텍스트 구성
    List<Store> candidates = storeRepository.findAllById(candidateIds);
    Map<Long, Store> storeMap = candidates.stream().collect(Collectors.toMap(Store::getId, s -> s));

    String userText = normalizeTags(userTags);
    List<String> storeTexts = new ArrayList<>();
    for (Long id : candidateIds) {
      Store s = storeMap.get(id);
      List<String> tags = storeTagMap.getOrDefault(id, List.of());
      storeTexts.add(buildStoreText(s, tags));
    }

    // 6) 임베딩 한 번에 계산 → 유사도 기반 재정렬
    List<Long> finalOrder = candidateIds;
    try {
      List<String> inputs = new ArrayList<>(1 + storeTexts.size());
      inputs.add(userText);
      inputs.addAll(storeTexts);

      List<double[]> vecs = openAIUtil.embedAll(inputs);
      if (vecs.size() == inputs.size()) {
        double[] u = vecs.get(0);
        List<Rank> ranks = new ArrayList<>();
        for (int i = 0; i < candidateIds.size(); i++) {
          double sim = OpenAIUtil.cosine(u, vecs.get(i + 1));
          ranks.add(new Rank(candidateIds.get(i), sim));
        }
        ranks.sort((a, b) -> Double.compare(b.sim, a.sim));
        finalOrder = ranks.stream().map(r -> r.id).toList();
      } else {
        log.warn("[Embeddings] size mismatch: expected={}, got={}", inputs.size(), vecs.size());
      }
    } catch (Exception e) {
      log.warn("[Embeddings] fallback to deterministic order. cause={}", e.toString());
    }

    // 7) 최종 응답(임베딩 재정렬 결과 기준)
    return finalOrder.stream()
        .map(storeMap::get)
        .filter(Objects::nonNull)
        .map(storeMapper::toResponse)
        .toList();
  }

  /**
   * 후보 5개 선발 규칙: 1) k1(해시태그 교집합) 내림차순 2) k2(미션 가중치) 내림차순 3) id 내림차순 4) k1>0 먼저 뽑고, 부족하면 (k1==0 &
   * k2>0)에서 채우고, 그래도 부족하면 나머지에서 채움
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

    // 1) k1>0 우선
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

    // 2) 부족하면 (k1==0 & k2>0)에서 채우기
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

    // 3) 그래도 부족하면 나머지에서 id 내림차순으로 채우기
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

  private Map<Long, Long> buildMissionStoreCount(List<Long> completedMissionIds) {
    if (completedMissionIds == null || completedMissionIds.isEmpty()) return Map.of();
    List<Mission> missions = missionRepository.findAllById(completedMissionIds);
    return missions.stream()
        .collect(Collectors.groupingBy(m -> m.getStore().getId(), Collectors.counting()));
  }

  private String normalizeTags(List<String> tags) {
    return tags.stream()
        .map(t -> t.startsWith("#") ? t.substring(1) : t)
        .collect(Collectors.joining(" "));
  }

  private String buildStoreText(Store s, List<String> tags) {
    String desc = (s != null && s.getDescription() != null) ? s.getDescription() : "";
    String tagStr = normalizeTags(tags);
    // 설명 + 태그를 합쳐 의미 임베딩 텍스트로 사용
    return (desc + " " + tagStr).trim();
  }

  private record Rank(Long id, double sim) {}
}
