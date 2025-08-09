package com.likelion.danchu.domain.store.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.likelion.danchu.domain.hashtag.entity.Hashtag;
import com.likelion.danchu.domain.hashtag.repository.HashtagRepository;
import com.likelion.danchu.domain.store.dto.request.StoreRequest;
import com.likelion.danchu.domain.store.dto.response.PageableResponse;
import com.likelion.danchu.domain.store.dto.response.StoreResponse;
import com.likelion.danchu.domain.store.entity.Store;
import com.likelion.danchu.domain.store.entity.StoreHashtag;
import com.likelion.danchu.domain.store.exception.StoreErrorCode;
import com.likelion.danchu.domain.store.mapper.StoreMapper;
import com.likelion.danchu.domain.store.repository.StoreHashtagRepository;
import com.likelion.danchu.domain.store.repository.StoreRepository;
import com.likelion.danchu.global.exception.CustomException;
import com.likelion.danchu.global.s3.entity.PathName;
import com.likelion.danchu.global.s3.service.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

  private final StoreRepository storeRepository;
  private final StoreMapper storeMapper;
  private final S3Service s3Service;
  private final HashtagRepository hashtagRepository;
  private final StoreHashtagRepository storeHashtagRepository;

  public StoreResponse createStore(StoreRequest storeRequest, MultipartFile imageFile) {
    // 주소로 가게 중복 체크
    if (storeRepository.existsByAddress(storeRequest.getAddress())) {
      throw new CustomException(StoreErrorCode.STORE_ALREADY_EXISTS);
    }

    // 인증 코드 중복 체크
    if (storeRepository.existsByAuthCode(storeRequest.getAuthCode())) {
      throw new CustomException(StoreErrorCode.STORE_AUTHCODE_DUPLICATED);
    }

    // 이미지 업로드 (PathName.STORE 폴더에 저장)
    String imageUrl;
    try {
      imageUrl = s3Service.uploadImage(PathName.STORE, imageFile).getImageUrl();
    } catch (Exception e) {
      throw new CustomException(StoreErrorCode.IMAGE_UPLOAD_FAILED);
    }

    // Store Entity 생성
    Store store = storeMapper.toEntity(storeRequest, imageUrl);

    // DB에 저장
    Store saved;
    try {
      saved = storeRepository.save(store);
    } catch (Exception e) {
      throw new CustomException(StoreErrorCode.STORE_SAVE_FAILED);
    }

    return storeMapper.toResponse(saved);
  }

  // 전체 가게 페이징 조회
  public PageableResponse<StoreResponse> getPaginatedStores(int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size); // 페이지당 3개
    Page<Store> storePage = storeRepository.findAll(pageRequest);
    Page<StoreResponse> storeResponsePage = storePage.map(storeMapper::toResponse);

    return PageableResponse.from(storeResponsePage);
  }

  // 가게 이름 검색
  public List<StoreResponse> searchStoresByKeyword(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      throw new CustomException(StoreErrorCode.EMPTY_KEYWORD);
    }
    List<Store> stores = storeRepository.findByNameContainingIgnoreCase(keyword);
    return storeMapper.toResponseList(stores);
  }

  // 가게 상세 조회
  public StoreResponse getStoreDetail(Long storeId) {
    Store store =
        storeRepository
            .findById(storeId)
            .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));
    return storeMapper.toResponse(store);
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
            .toList();

    // DB에서 해당 해시태그 이름을 가진 해시태그 엔티티들 조회
    List<Hashtag> hashtags = hashtagRepository.findByNameIn(formattedTags);
    if (hashtags.isEmpty()) {
      return PageableResponse.from(Page.empty(PageRequest.of(page, size)));
    }

    // 해시태그들과 연결된 StoreHashtag 리스트 조회
    List<StoreHashtag> storeHashtags = storeHashtagRepository.findByHashtagIn(hashtags);

    // 포함된 해시태그 수 맵핑
    Map<Long, Long> storeIdCountMap =
        storeHashtags.stream()
            .collect(
                Collectors.groupingBy(
                    StoreHashtag -> StoreHashtag.getStore().getId(), Collectors.counting()));

    // 해시태그 수와 정확히 일치하는 storeId만 추출
    int requiredTagCount = hashtags.size();
    List<Long> matchedStores =
        storeIdCountMap.entrySet().stream()
            .filter(entry -> entry.getValue() == requiredTagCount)
            .map(Map.Entry::getKey)
            .toList();

    // 페이징 처리된 Store 조회
    PageRequest pageRequest = PageRequest.of(page, size);
    Page<Store> storePage = storeRepository.findDistinctByIdIn(matchedStores, pageRequest);
    Page<StoreResponse> responsePage = storePage.map(storeMapper::toResponse);

    return PageableResponse.from(responsePage);
  }
}
