package com.likelion.danchu.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "StoreDistanceResponse", description = "거리 포함 가게 응답")
public class StoreDistanceResponse {

  @Schema(description = "가게 기본 정보")
  private StoreResponse store;

  @Schema(description = "현재 위치로부터 거리(미터)", example = "152.3")
  private Double distanceMeters;

  @Schema(description = "현재 위치로부터 거리(킬로미터)", example = "0.15")
  private Double distanceKm;
}
