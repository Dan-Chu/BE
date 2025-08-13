package com.likelion.danchu.domain.store.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.danchu.domain.hashtag.dto.request.HashtagRequest;
import com.likelion.danchu.domain.hashtag.dto.response.HashtagResponse;
import com.likelion.danchu.domain.hashtag.entity.Hashtag;
import com.likelion.danchu.domain.hashtag.exception.HashtagErrorCode;
import com.likelion.danchu.domain.hashtag.mapper.HashtagMapper;
import com.likelion.danchu.domain.hashtag.repository.HashtagRepository;
import com.likelion.danchu.domain.store.dto.response.PageableResponse;
import com.likelion.danchu.domain.store.dto.response.StoreResponse;
import com.likelion.danchu.domain.store.entity.Store;
import com.likelion.danchu.domain.store.entity.StoreHashtag;
import com.likelion.danchu.domain.store.exception.StoreErrorCode;
import com.likelion.danchu.domain.store.mapper.StoreMapper;
import com.likelion.danchu.domain.store.repository.StoreHashtagRepository;
import com.likelion.danchu.domain.store.repository.StoreRepository;
import com.likelion.danchu.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreHashtagService {

  private final HashtagRepository hashtagRepository;
  private final HashtagMapper hashtagMapper;
  private final StoreRepository storeRepository;
  private final StoreHashtagRepository storeHashtagRepository;
  private final StoreMapper storeMapper;

  /**
   * 특정 가게에 해시태그를 생성/연결하는 서비스 메서드
   *
   * @param storeId 해시태그를 등록할 대상 가게 ID
   * @param request 해시태그 생성 요청 DTO
   * @return 생성 또는 재사용된 해시태그 응답 DTO
   * @throws CustomException 해시태그 길이가 유효하지 않거나, 가게를 찾을 수 없거나, 이미 해당 가게에 동일한 해시태그가 연결된 경우
   */
  public HashtagResponse createHashtagForStore(Long storeId, HashtagRequest request) {
    final int MAX_HASHTAGS_PER_STORE = 4;

    String name = request.toFormattedName();
    if (name.length() < 2 || name.length() > 11) {
      throw new CustomException(HashtagErrorCode.HASHTAG_LENGTH_INVALID);
    }

    // 사전 개수 검증: 이미 4개면 차단
    long current = storeHashtagRepository.countByStore_Id(storeId);
    if (current >= MAX_HASHTAGS_PER_STORE) {
      throw new CustomException(HashtagErrorCode.HASHTAG_LIMIT_EXCEEDED);
    }

    // 해시태그 재사용 or 생성
    Hashtag hashtag =
        hashtagRepository
            .findByName(name)
            .orElseGet(() -> hashtagRepository.save(Hashtag.builder().name(name).build()));

    // 가게 조회
    Store store =
        storeRepository
            .findById(storeId)
            .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

    // 중복 연결 방지
    if (storeHashtagRepository.existsByStoreAndHashtag(store, hashtag)) {
      throw new CustomException(HashtagErrorCode.HASHTAG_ALREADY_EXISTS);
    }

    // 저장
    storeHashtagRepository.save(StoreHashtag.builder().store(store).hashtag(hashtag).build());

    // 사후 재검증(동시성 대비): 초과 시 롤백
    long after = storeHashtagRepository.countByStore_Id(storeId);
    if (after > MAX_HASHTAGS_PER_STORE) {
      throw new CustomException(HashtagErrorCode.HASHTAG_LIMIT_EXCEEDED);
    }

    return hashtagMapper.toResponse(hashtag);
  }

  /**
   * 선택된 해시태그 목록(tags)에 해당하는 가게들을 페이징 조회합니다.
   *
   * @param tags 사용자가 선택한 해시태그 문자열 리스트 (예: ["치킨", "조용한"])
   * @param page 페이지 번호 (0부터 시작)
   * @param size 한 페이지에 포함될 가게 수
   * @return PageableResponse<StoreResponse> 페이징된 가게 목록 응답
   * @throws CustomException 해시태그 목록이 비어 있거나 존재하지 않는 경우
   */
  public PageableResponse<StoreResponse> filterStoresByHashtags(
      List<String> tags, int page, int size) {

    // 해시태그가 없을 때 예외 발생
    if (tags == null || tags.isEmpty()) {
      throw new CustomException(StoreErrorCode.EMPTY_HASHTAG_LIST);
    }

    // 해시태그 문자열을 포맷팅 (공백 제거, 소문자 변환, '#' 자동 붙이기)
    List<String> formattedTags =
        tags.stream()
            .map(
                tag -> {
                  String cleaned = tag.trim().replaceAll("\\s+", "").toLowerCase();
                  return cleaned.startsWith("#") ? cleaned : "#" + cleaned;
                })
            .distinct()
            .toList();

    // DB에서 해당 해시태그 이름을 가진 해시태그 엔티티들 조회
    List<Hashtag> hashtags = hashtagRepository.findByNameIn(formattedTags);
    PageRequest pageable = PageRequest.of(page, size);

    // 요청한 해시태그 중 하나라도 없으면 400
    var existingNames = hashtags.stream().map(Hashtag::getName).collect(Collectors.toSet());
    var missingHashtags =
        formattedTags.stream().filter(hashtag -> !existingNames.contains(hashtag)).toList();
    if (!missingHashtags.isEmpty()) {
      throw new CustomException(HashtagErrorCode.HASHTAG_NOT_FOUND);
    }

    // 해시태그들과 연결된 StoreHashtag 리스트 조회
    List<StoreHashtag> storeHashtags = storeHashtagRepository.findByHashtagIn(hashtags);

    // 각 store가 선택한 모든 태그를 포함하는지 계산
    Map<Long, Long> storeIdToDistinctTagCount =
        storeHashtags.stream()
            .collect(
                Collectors.groupingBy(
                    storeHashtag -> storeHashtag.getStore().getId(),
                    Collectors.mapping(
                        storeHashtag -> storeHashtag.getHashtag().getId(),
                        Collectors.collectingAndThen(
                            Collectors.toSet(), store -> (long) store.size()))));

    int requiredTagCount = hashtags.size();
    List<Long> matchedStoreIds =
        storeIdToDistinctTagCount.entrySet().stream()
            .filter(e -> e.getValue() == requiredTagCount)
            .map(Map.Entry::getKey)
            .toList();

    if (matchedStoreIds.isEmpty()) {
      return PageableResponse.from(Page.empty(pageable));
    }

    // 매칭된 가게들 페이징 조회
    Page<Store> storePage = storeRepository.findDistinctByIdIn(matchedStoreIds, pageable);
    if (storePage.isEmpty()) {
      return PageableResponse.from(Page.empty(pageable));
    }

    // 현재 페이지의 가게들에 대해서만 해시태그를 한 번에 조회 → 매핑
    List<Long> pageStoreIds = storePage.getContent().stream().map(Store::getId).toList();
    List<StoreHashtag> pageRelations = storeHashtagRepository.findByStore_IdIn(pageStoreIds);

    Map<Long, List<HashtagResponse>> hashtagsByStoreId =
        pageRelations.stream()
            .collect(
                Collectors.groupingBy(
                    storeHashtag -> storeHashtag.getStore().getId(),
                    Collectors.mapping(
                        storeHashtag -> hashtagMapper.toResponse(storeHashtag.getHashtag()),
                        Collectors.toList())));

    // 해시태그 포함 DTO로 변환
    Page<StoreResponse> responsePage =
        storePage.map(
            store ->
                storeMapper.toResponse(
                    store, hashtagsByStoreId.getOrDefault(store.getId(), List.of())));

    return PageableResponse.from(responsePage);
  }
}
