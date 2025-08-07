package com.likelion.danchu.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
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
@Schema(title = "LoginRequest DTO", description = "사용자 로그인을 위한 요청")
public class LoginRequest {

  @Email
  @NotBlank(message = "사용자 이메일 항목은 필수입니다.")
  @Schema(description = "이메일 주소", example = "danchu@skuniv.ac.kr")
  private String email;

  @NotBlank(message = "비밀번호 항목은 필수입니다.")
  @Schema(description = "비밀번호", example = "qwer1234!")
  private String password;
}
