package com.likelion.danchu.domain.stamp.entity;

import io.swagger.v3.oas.annotations.media.Schema;

public enum StampStatus {
  @Schema(description = "적립 진행중")
  IN_PROGRESS,
  @Schema(description = "쿠폰 수령 대기")
  READY_TO_CLAIM,
}
