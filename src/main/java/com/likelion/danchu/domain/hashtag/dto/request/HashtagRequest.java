package com.likelion.danchu.domain.hashtag.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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

  @Schema(
      description = "해시태그 이름 (예: '조용한') - '#' 없이 입력하면 자동으로 붙여지고, 영어는 모두 소문자로 변환됩니다.",
      example = "조용한")
  @NotBlank(message = "해시태그 이름은 비어 있을 수 없습니다.")
  @Size(min = 1, max = 10, message = "해시태그는 최소 1자 이상, 최대 10자 이하로 입력해주세요.")
  private String name;

  public String toFormattedName() {
    String raw = name.trim().replaceAll("\\s+", "").toLowerCase(); // 공백 제거 및 소문자 변환
    return raw.startsWith("#") ? raw : '#' + raw; // 자동 붙이기
  }
}
