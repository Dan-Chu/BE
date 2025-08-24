package com.likelion.danchu.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "StoreListItemResponse", description = "목록 아이템(가게 정보를 store로 래핑)")
public class StoreListItemResponse {

  @Schema(description = "가게 정보", requiredMode = Schema.RequiredMode.REQUIRED)
  private StoreResponse store;
}
