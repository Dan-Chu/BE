package com.likelion.danchu.domain.menu.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.likelion.danchu.domain.menu.dto.request.MenuRequest;
import com.likelion.danchu.domain.menu.dto.response.MenuResponse;
import com.likelion.danchu.domain.menu.entity.Menu;
import com.likelion.danchu.domain.menu.exception.MenuErrorCode;
import com.likelion.danchu.domain.menu.mapper.MenuMapper;
import com.likelion.danchu.domain.menu.repository.MenuRepository;
import com.likelion.danchu.domain.store.entity.Store;
import com.likelion.danchu.domain.store.repository.StoreRepository;
import com.likelion.danchu.global.exception.CustomException;
import com.likelion.danchu.infra.s3.entity.PathName;
import com.likelion.danchu.infra.s3.service.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {

  private final StoreRepository storeRepository;
  private final MenuRepository menuRepository;
  private final MenuMapper menuMapper;
  private final S3Service s3Service;

  /**
   * 새로운 메뉴 생성=
   *
   * @param storeId 가게 ID
   * @param request 메뉴 생성 요청 DTO
   * @param imageFile 업로드 이미지(선택)
   */
  public MenuResponse createMenu(Long storeId, MenuRequest request, MultipartFile imageFile) {

    // 가게 존재 확인
    Store store =
        storeRepository
            .findById(storeId)
            .orElseThrow(
                () ->
                    new CustomException(
                        com.likelion.danchu.domain.store.exception.StoreErrorCode.STORE_NOT_FOUND));

    // 동일 가게 내 메뉴명 중복 방지
    if (menuRepository.existsByStore_IdAndName(storeId, request.getName())) {
      throw new CustomException(MenuErrorCode.DUPLICATE_MENU_NAME);
    }

    // (선택) 이미지 업로드
    String uploadedUrl = null;
    if (imageFile != null && !imageFile.isEmpty()) {
      try {
        uploadedUrl = s3Service.uploadImage(PathName.MENU, imageFile).getImageUrl();
      } catch (Exception e) {
        throw new CustomException(MenuErrorCode.MENU_UPLOAD_FAILED);
      }
    }

    Menu saved = menuRepository.save(menuMapper.toEntity(request, store, uploadedUrl));
    return menuMapper.toResponse(saved);
  }

  /**
   * 가게의 메뉴 전체 목록 조회
   *
   * @param storeId 대상 가게 ID
   * @return {@link MenuResponse} 리스트 (가격은 price, priceFormatted 동시 제공)
   * @throws CustomException {@code STORE_NOT_FOUND} - 가게 미존재 시
   */
  @Transactional
  public List<MenuResponse> getMenus(Long storeId) {
    // 가게 존재 확인
    if (!storeRepository.existsById(storeId)) {
      throw new CustomException(
          com.likelion.danchu.domain.store.exception.StoreErrorCode.STORE_NOT_FOUND);
    }
    // id 오름차순 정렬로 전체 반환
    return menuRepository.findByStore_IdOrderByIdAsc(storeId).stream()
        .map(menuMapper::toResponse) // priceFormatted 포함됨
        .toList();
  }
}
