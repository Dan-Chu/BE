package com.likelion.danchu.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "StoreResponse DTO", description = "가게 관련 응답")
public class StoreResponse {}
