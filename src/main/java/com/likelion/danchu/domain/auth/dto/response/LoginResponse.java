package com.likelion.danchu.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "LoginResponseDTO", description = "사용자 로그인에 대한 응답 반환")
public class LoginResponse {

  @Schema(description = "사용자 Access Token")
  private String accessToken;

  @Schema(description = "사용자 ID", example = "1")
  private Long userId;

  @Schema(description = "사용자 이메일", example = "naooung@naver.com")
  private String email;

  @Schema(description = "사용자 Access Token 만료 시간", example = "1000000")
  private Long expirationTime;
}
