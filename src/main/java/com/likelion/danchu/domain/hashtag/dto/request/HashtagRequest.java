package com.likelion.danchu.domain.hashtag.dto.request;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "HashtagRequest DTO", description = "해시태그 관련 요청")
public class HashtagRequest {

  @Schema(description = "해시태그 이름 (예: '조용한') - '#' 없이 입력하면 자동으로 붙여집니다.", example = "조용한")
  @NotBlank(message = "해시태그 이름은 비어 있을 수 없습니다.")
  private String name;
}
