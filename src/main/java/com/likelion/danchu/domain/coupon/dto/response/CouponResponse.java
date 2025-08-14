package com.likelion.danchu.domain.coupon.dto.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "CouponResponse DTO", description = "쿠폰 응답 정보")
public class CouponResponse {

  @Schema(description = "쿠폰 ID", example = "1")
  private Long id;

  @Schema(description = "가게 이름", example = "르아브르")
  private String storeName;

  @Schema(description = "쿠폰 내용", example = "아메리카노 1잔 무료")
  private String reward;

  @Schema(
      description = "쿠폰 이미지 URL",
      example = "https://danchu.s3.ap-northeast-2.amazonaws.com/coupon.jpg")
  private String imageUrl;

  @Schema(description = "사용 기한", example = "2025-12-25")
  private LocalDate expirationDate;

  @Schema(description = "쿠폰 사용을 위한 인증번호", example = "0000")
  private String authCode;
}
