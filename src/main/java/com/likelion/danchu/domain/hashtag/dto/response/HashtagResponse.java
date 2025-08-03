package com.likelion.danchu.domain.hashtag.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "HashtagResponse DTO", description = "해시태그 관련 응답")
public class HashtagResponse {

  @Schema(description = "해시태그 ID", example = "1")
  private Long id;

  @Schema(description = "저장된 해시태그 이름", example = "#조용한")
  private String name;
}
