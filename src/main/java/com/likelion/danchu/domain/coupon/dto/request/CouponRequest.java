package com.likelion.danchu.domain.coupon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "CouponRequest DTO", description = "쿠폰 관련 요청")
public class CouponRequest {

  @NotNull(message = "가게 ID는 필수입니다.")
  private Long storeId;

  @NotBlank(message = "쿠폰 내용은 필수입니다.")
  private String reward;

  @NotBlank(message = "이미지 URL은 필수입니다.")
  private String imageUrl;
}
