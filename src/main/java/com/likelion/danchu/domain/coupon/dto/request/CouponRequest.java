package com.likelion.danchu.domain.coupon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(title = "CouponRequest DTO", description = "쿠폰 관련 요청")
public class CouponRequest {

  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  @Schema(name = "CreateRequest", description = "쿠폰 생성 요청")
  public static class CreateRequest {

    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "쿠폰을 발급할 가게 ID", example = "1")
    private Long storeId;

    @NotBlank(message = "쿠폰 내용은 필수입니다.")
    @Schema(description = "쿠폰 내용", example = "무료 아메리카노 1잔")
    private String reward;
  }
}
