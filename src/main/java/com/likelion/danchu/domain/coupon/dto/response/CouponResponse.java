package com.likelion.danchu.domain.coupon.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "CouponResponse DTO", description = "쿠폰 관련 응답")
public class CouponResponse {}
