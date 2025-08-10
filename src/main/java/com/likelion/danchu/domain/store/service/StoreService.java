package com.likelion.danchu.domain.store.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.likelion.danchu.domain.hashtag.dto.response.HashtagResponse;
import com.likelion.danchu.domain.hashtag.mapper.HashtagMapper;
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
  private final HashtagMapper hashtagMapper;

  /**
   * 새로운 가게를 생성합니다.
   *
   * @param storeRequest 가게 생성 요청 DTO
   * @param imageFile 업로드할 메인 이미지 파일
   * @return 생성된 가게 정보
   * @throws CustomException 주소 또는 인증 코드가 중복되거나, 이미지 업로드/저장 실패 시 발생
   */
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
    final String imageUrl;
    try {
      imageUrl = s3Service.uploadImage(PathName.STORE, imageFile).getImageUrl();
    } catch (Exception e) {
      throw new CustomException(StoreErrorCode.IMAGE_UPLOAD_FAILED);
    }

    Store store = storeMapper.toEntity(storeRequest, imageUrl);
    try {
      Store saved = storeRepository.save(store);
      // 생성 직후엔 해시태그가 없으니 빈 배열로 반환
      return storeMapper.toResponse(saved, List.of());
    } catch (Exception e) {
      throw new CustomException(StoreErrorCode.STORE_SAVE_FAILED);
    }
  }

  /**
   * 전체 가게 목록을 페이징 조회합니다. 각 가게의 해시태그를 함께 반환합니다.
   *
   * @param page 페이지 번호 (0부터 시작)
   * @param size 한 페이지에 포함될 가게 수
   * @return 페이징된 가게 목록 응답
   */
  public PageableResponse<StoreResponse> getPaginatedStores(int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size); // 페이지당 3개
    Page<Store> storePage = storeRepository.findAll(pageRequest);
    List<Store> stores = storePage.getContent();

    if (stores.isEmpty()) {
      return PageableResponse.from(storePage.map(storeMapper::toResponse)); // 그대로 빈 페이지 반환
    }

    // 현재 페이지의 가게 ID들 수집
    List<Long> storeIds = stores.stream().map(Store::getId).toList();

    // 모든 해시태그 연결 한번에 조회
    List<StoreHashtag> relations = storeHashtagRepository.findByStore_IdIn(storeIds);

    // 해시태그 응답 리스트로 매팅
    Map<Long, List<HashtagResponse>> hashtagsByStoreId =
        relations.stream()
            .collect(
                Collectors.groupingBy(
                    storeHashtag -> storeHashtag.getStore().getId(),
                    Collectors.mapping(
                        storeHashtag -> hashtagMapper.toResponse(storeHashtag.getHashtag()),
                        Collectors.toList())));

    // 각 가게 별로 해시태그 포함하여 DTO 변환
    Page<StoreResponse> storeResponsePage =
        storePage.map(
            store ->
                storeMapper.toResponse(
                    store, hashtagsByStoreId.getOrDefault(store.getId(), List.of())));

    return PageableResponse.from(storeResponsePage);
  }

  /**
   * 검색어(keyword)가 가게 이름에 포함된 가게들을 페이징 조회합니다. 각 가게의 해시태그를 함께 반환합니다.
   *
   * @param keyword 검색 키워드
   * @param page 페이지 번호 (0부터 시작)
   * @param size 한 페이지에 포함될 가게 수
   * @return 페이징된 가게 목록 응답
   * @throws CustomException 검색어가 비어 있는 경우
   */
  public PageableResponse<StoreResponse> searchStoresByKeyword(String keyword, int page, int size) {
    if (keyword == null || keyword.trim().isEmpty()) {
      throw new CustomException(StoreErrorCode.EMPTY_KEYWORD);
    }
    Pageable pageable = PageRequest.of(page, size);
    Page<Store> storePage =
        storeRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
    List<Store> stores = storePage.getContent();

    if (stores.isEmpty()) {
      return PageableResponse.from(storePage.map(storeMapper::toResponse)); // 빈 페이지 그대로
    }

    // 현재 페이지의 가게 ID 모으기
    List<Long> storeIds = stores.stream().map(Store::getId).toList();

    // 해당 가게들의 해시태그 관계 한 번에 조회
    List<StoreHashtag> relations = storeHashtagRepository.findByStore_IdIn(storeIds);

    // List<HashtagResponse> 매핑
    Map<Long, List<HashtagResponse>> hashtagsByStoreId =
        relations.stream()
            .collect(
                Collectors.groupingBy(
                    storeHashtag -> storeHashtag.getStore().getId(),
                    Collectors.mapping(
                        storeHashtag -> hashtagMapper.toResponse(storeHashtag.getHashtag()),
                        Collectors.toList())));
    // 해시태그 포함해 DTO로 변환
    Page<StoreResponse> responsePage =
        storePage.map(
            store ->
                storeMapper.toResponse(
                    store, hashtagsByStoreId.getOrDefault(store.getId(), List.of())));

    return PageableResponse.from(responsePage);
  }

  /**
   * 특정 가게의 상세 정보를 조회합니다. 해당 가게의 해시태그를 함께 반환합니다.
   *
   * @param storeId 조회할 가게 ID
   * @return 가게 상세 정보 응답
   * @throws CustomException 가게가 존재하지 않는 경우
   */
  public StoreResponse getStoreDetail(Long storeId) {
    Store store =
        storeRepository
            .findById(storeId)
            .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

    // 가게 해시태그 목록 조회
    List<StoreHashtag> relations = storeHashtagRepository.findByStore_Id(storeId);
    List<HashtagResponse> hashtagResponses =
        relations.stream().map(StoreHashtag::getHashtag).map(hashtagMapper::toResponse).toList();

    return storeMapper.toResponse(store, hashtagResponses);
  }
}
