package com.likelion.danchu.domain.store.service;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.likelion.danchu.domain.store.dto.request.StoreRequest;
import com.likelion.danchu.domain.store.dto.response.PageableResponse;
import com.likelion.danchu.domain.store.dto.response.StoreResponse;
import com.likelion.danchu.domain.store.entity.Store;
import com.likelion.danchu.domain.store.exception.StoreErrorCode;
import com.likelion.danchu.domain.store.mapper.StoreMapper;
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
}
