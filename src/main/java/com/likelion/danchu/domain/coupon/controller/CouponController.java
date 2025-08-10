package com.likelion.danchu.domain.coupon.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.likelion.danchu.domain.coupon.dto.request.CouponRequest;
import com.likelion.danchu.domain.coupon.dto.response.CouponResponse;
import com.likelion.danchu.domain.coupon.service.CouponService;
import com.likelion.danchu.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
@Tag(name = "Coupon", description = "Coupon 관련 API")
public class CouponController {

  private final CouponService couponService;

  @Operation(
      summary = "쿠폰 생성",
      description =
          """
        새로운 쿠폰을 생성합니다. (로그인 필요)

        ✅ 요청 형식 (multipart/form-data)
        - **couponRequest**: 쿠폰 정보(JSON)
        - **image**: 쿠폰 이미지 파일 (**필수**)

        ✅ 유의사항
        - 이미지는 S3 업로드 후 URL로 저장됩니다.
        - storeId는 유효한 가게 ID여야 합니다.
        """)
  @PostMapping(
      path = "",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BaseResponse<CouponResponse>> createCoupon(
      @Parameter(
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE),
              description = "쿠폰 정보(JSON)")
          @RequestPart("couponRequest")
          @Valid
          CouponRequest.CreateRequest couponRequest,
      @Parameter(
              content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE),
              description = "쿠폰 이미지")
          @RequestPart(value = "image", required = true)
          MultipartFile imageFile) {

    CouponResponse response = couponService.createCoupon(couponRequest, imageFile);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success("쿠폰 생성이 완료되었습니다.", response));
  }

  @Operation(
      summary = "쿠폰 전체 조회 (만료 제외, 임박순)",
      description =
          """
      만료되지 않은 모든 쿠폰을 **만료일이 가까운 순서**로 조회합니다.
      - 만료일(expirationDate)이 오늘 이후인 쿠폰만 반환합니다.
      - 당장 소멸될 쿠폰부터 보여줍니다.
      """)
  @GetMapping
  public ResponseEntity<BaseResponse<List<CouponResponse>>> getAllCoupons() {
    List<CouponResponse> responses = couponService.getAllValidCoupons();
    return ResponseEntity.status(HttpStatus.OK)
        .body(BaseResponse.success("쿠폰 목록을 조회했습니다.", responses));
  }
}
