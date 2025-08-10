package com.likelion.danchu.domain.coupon.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "CouponResponse DTO", description = "쿠폰 응답 정보")
public class CouponResponse {

  @Schema(description = "쿠폰 ID", example = "1")
  private Long id;

  @Schema(description = "사용자 ID", example = "10")
  private Long userId;

  @Schema(description = "가게 ID", example = "5")
  private Long storeId;

  @Schema(description = "쿠폰 내용", example = "아메리카노 1잔 무료")
  private String reward;

  @Schema(
      description = "쿠폰 이미지 URL",
      example = "https://danchu.s3.ap-northeast-2.amazonaws.com/coupon.jpg")
  private String imageUrl;

  @Schema(description = "사용 기한", example = "2025-12-25")
  private LocalDate expirationDate;
}
