package com.likelion.danchu.domain.store.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.likelion.danchu.domain.store.dto.request.StoreRequest;
import com.likelion.danchu.domain.store.dto.response.PageableResponse;
import com.likelion.danchu.domain.store.dto.response.StoreResponse;
import com.likelion.danchu.domain.store.exception.StoreErrorCode;
import com.likelion.danchu.domain.store.service.StoreService;
import com.likelion.danchu.global.exception.CustomException;
import com.likelion.danchu.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
@Tag(name = "Store", description = "Store 관련 API")
public class StoreController {

  private final StoreService storeService;

  @Operation(
      summary = "가게 등록",
      description =
          """
              새로운 가게 정보를 등록합니다.

              ✅ 필수 입력값:
              - 가게 이름: **1자 이상, 10자 이내**
              - 주소: **1자 이상, 50자 이내**
              - 가게 소개: **1자 이상, 200자 이내**
              - 전화번호: **010-1234-5678** 또는 **02-345-6789** 형식
              - 오픈/마감 시간: **HH:mm** 형식 (예: 09:00, 21:00)
              - 인증 코드: **숫자 4자리 (예: 0123)**
              - 이미지 파일: **multipart/form-data** 형식

              ✅ 유의사항:
              - 동일한 주소의 가게는 중복 등록할 수 없습니다.
              - 인증 코드는 가게마다 고유해야 하며, 중복될 수 없습니다.
              """)
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseResponse<StoreResponse>> createStore(
      @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
          @RequestPart("storeRequest")
          @Valid
          StoreRequest storeRequest,
      @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
          @RequestPart("image")
          MultipartFile imageFile) {

    if (imageFile == null || imageFile.isEmpty()) {
      throw new CustomException(StoreErrorCode.IMAGE_UPLOAD_FAILED);
    }

    StoreResponse storeResponse = storeService.createStore(storeRequest, imageFile);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success("가게 생성에 성공했습니다.", storeResponse));
  }

  @Operation(
      summary = "가게 목록 페이징 조회",
      description =
          """
              페이지 번호(page)와 사이즈(size)를 기반으로 전체 가게를 페이징 조회합니다.

              ✅ 기본 설정:
              - **한 페이지당 3개의 가게**가 표시됩니다.

              ✅ 파라미터 설명:
              - page : 0부터 시작하는 페이지 번호입니다. (예: 첫 번째 페이지 → page=0)
              - size : 페이지 당 보여줄 가게 수입니다. (기본값: 3)
              """)
  @GetMapping
  public ResponseEntity<BaseResponse<PageableResponse<StoreResponse>>> getPaginatedStores(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "3") int size) {

    PageableResponse<StoreResponse> storeResponses = storeService.getPaginatedStores(page, size);
    return ResponseEntity.ok(BaseResponse.success("가게 페이징 조회에 성공했습니다.", storeResponses));
  }
}
