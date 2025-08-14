package com.likelion.danchu.domain.stamp.dto.request;

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
@Schema(title = "StampRequest DTO", description = "스탬프 관련 요청")
public class StampRequest {

  @NotBlank(message = "인증 코드는 필수입니다.")
  @Schema(description = "가게 인증코드", example = "0000")
  private String authCode;
}
