package com.likelion.danchu.domain.store.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.likelion.danchu.domain.hashtag.dto.request.HashtagRequest;
import com.likelion.danchu.domain.hashtag.dto.response.HashtagResponse;
import com.likelion.danchu.domain.store.dto.request.StoreRequest;
import com.likelion.danchu.domain.store.dto.response.PageableResponse;
import com.likelion.danchu.domain.store.dto.response.StoreDistanceResponse;
import com.likelion.danchu.domain.store.dto.response.StoreListItemResponse;
import com.likelion.danchu.domain.store.dto.response.StoreResponse;
import com.likelion.danchu.domain.store.exception.StoreErrorCode;
import com.likelion.danchu.domain.store.service.StoreHashtagService;
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
  private final StoreHashtagService storeHashtagService;

  @Operation(
      summary = "가게 등록",
      description =
          """
              새로운 가게 정보를 등록합니다.
              - 실제 도로명 주소를 입력해야 주소가 위도/경도로 변환됩니다.

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
  public ResponseEntity<BaseResponse<PageableResponse<StoreListItemResponse>>> getPaginatedStores(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "3") int size) {

    PageableResponse<StoreListItemResponse> storeResponses =
        storeService.getPaginatedStores(page, size);
    return ResponseEntity.ok(BaseResponse.success("가게 페이징 조회에 성공했습니다.", storeResponses));
  }

  @Operation(
      summary = "가게 이름 검색",
      description =
          """
              검색어(keyword)를 기반으로 가게 이름에 해당 키워드가 포함된 가게 목록을 조회합니다.
              - 요청에 위도(lat), 경도(lng)를 함께 전달하면 결과가 거리순(가까운 순)으로 정렬됩니다.
              """)
  @GetMapping("/search")
  public ResponseEntity<BaseResponse<PageableResponse<StoreDistanceResponse>>>
      searchStoresByKeyword(
          @RequestParam String keyword,
          @Parameter(description = "사용자 위도", example = "37.4881292540693")
              @RequestParam(required = false)
              Double lat,
          @Parameter(description = "사용자 경도", example = "127.111066039437")
              @RequestParam(required = false)
              Double lng,
          @Parameter(description = "페이지 번호(0부터 시작)", example = "0")
              @RequestParam(defaultValue = "0")
              int page,
          @Parameter(description = "페이지 크기", example = "3") @RequestParam(defaultValue = "3")
              int size) {
    PageableResponse<StoreDistanceResponse> storeResponse =
        storeService.searchStoresByKeyword(keyword, page, size, lat, lng);

    return ResponseEntity.ok(BaseResponse.success("가게 검색에 성공했습니다.", storeResponse));
  }

  @Operation(summary = "가게 상세 조회", description = "storeId를 기반으로 특정 가게의 상세 정보를 페이징 조회합니다.")
  @GetMapping("/{storeId}")
  public ResponseEntity<BaseResponse<StoreResponse>> getStoreDetail(
      @Parameter(description = "가게 ID", example = "1") @PathVariable Long storeId) {
    StoreResponse storeResponse = storeService.getStoreDetail(storeId);
    return ResponseEntity.ok(BaseResponse.success("가게 상세 조회에 성공했습니다.", storeResponse));
  }

  @Operation(
      summary = "특정 가게 해시태그 등록",
      description =
          """
              특정 가게에 해시태그를 등록합니다.

              - 해시태그 이름은 **'#' 없이 입력해도 자동으로 붙여집니다**.
              - 영어는 **모두 소문자로 변환**되어 저장됩니다.
              - 이름은 **비어 있을 수 없으며**, 최소 1자 이상, 최대 10자 이하로 입력해야 합니다.
              """)
  @PostMapping("/{storeId}/hashtags")
  public ResponseEntity<BaseResponse<HashtagResponse>> createHashtagForStore(
      @Parameter(name = "storeId", description = "가게 ID", example = "1", required = true)
          @PathVariable
          Long storeId,
      @Valid @RequestBody HashtagRequest hashtagRequest) {
    HashtagResponse hashtagResponse =
        storeHashtagService.createHashtagForStore(storeId, hashtagRequest);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success("해시태그 생성에 성공했습니다.", hashtagResponse));
  }

  @Operation(
      summary = "해시태그 기반 가게 필터링",
      description =
          """
              선택한 해시태그를 모두 포함하는 가게 목록을 페이징 조회합니다.

              - 400: Hashtags 파라미터가 비었거나 유효하지 않음
              - 404: 존재하지 않는 해시태그가 포함됨
              - 먼저 /api/hashtags로 확인하고 사용하세요.
              - 요청에 위도(lat), 경도(lng)를 함께 전달하면 결과가 거리순(가까운 순)으로 정렬됩니다.
              """)
  @GetMapping("/filter")
  public ResponseEntity<BaseResponse<PageableResponse<StoreDistanceResponse>>>
      filterStoresByHashtags(
          @RequestParam List<String> tags,
          @Parameter(description = "사용자 위도", example = "37.4881292540693")
              @RequestParam(required = false)
              Double lat,
          @Parameter(description = "사용자 경도", example = "127.111066039437")
              @RequestParam(required = false)
              Double lng,
          @Parameter(description = "페이지 번호(0부터 시작)", example = "0")
              @RequestParam(defaultValue = "0")
              int page,
          @Parameter(description = "페이지 크기", example = "3") @RequestParam(defaultValue = "3")
              int size) {
    PageableResponse<StoreDistanceResponse> result =
        storeHashtagService.filterStoresByHashtags(tags, page, size, lat, lng);
    return ResponseEntity.ok(BaseResponse.success("가게 해시태그 필터 조회 성공했습니다.", result));
  }

  @Operation(
      summary = "현재 위치 기준, 거리순 가게 페이징 조회",
      description =
          """
              사용자 좌표(lat, lng)를 기준으로 가까운 순으로 가게를 페이징 조회합니다.

              ✅ 기본 단위
              - distanceMeters: 미터(m)
              - distanceKm: 킬로미터(km)

              ✅ radius(옵션)
              - 단위: 미터(m)
              - 예: radius=3000 → 3km 이내만 조회
              """)
  @GetMapping("/nearby")
  public ResponseEntity<BaseResponse<PageableResponse<StoreDistanceResponse>>> getNearbyStores(
      @Parameter(description = "사용자 현재 위도", required = true, example = "37.4881292540693")
          @RequestParam
          double lat,
      @Parameter(description = "사용자 현재 경도", required = true, example = "127.111066039437")
          @RequestParam
          double lng,
      @Parameter(description = "페이지 번호(0부터)", example = "0") @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "페이지 크기", example = "3") @RequestParam(defaultValue = "3") int size,
      @Parameter(description = "검색 반경(미터), 미입력 시 제한 없음") @RequestParam(required = false)
          Double radius) {

    PageableResponse<StoreDistanceResponse> result =
        storeService.getNearbyStores(lat, lng, page, size, radius);

    return ResponseEntity.ok(BaseResponse.success("현재 위치 기준 거리순 가게 조회에 성공했습니다.", result));
  }

  @Operation(summary = "가게 삭제", description = "storeId로 가게를 삭제합니다.")
  @DeleteMapping("/{storeId}")
  public ResponseEntity<BaseResponse> deleteStore(
      @Parameter(description = "가게 ID", example = "1") @PathVariable Long storeId) {
    storeService.deleteStore(storeId);
    return ResponseEntity.ok(BaseResponse.success("가게 삭제에 성공했습니다."));
  }
}
