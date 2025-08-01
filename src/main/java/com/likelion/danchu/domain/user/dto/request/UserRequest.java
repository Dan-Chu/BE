package com.likelion.danchu.domain.user.dto.request;

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
@Schema(title = "UserRequest DTO", description = "사용자 관련 요청")
public class UserRequest {

  @NotBlank(message = "닉네임은 필수입니다.")
  private String nickname;

  @Email(message = "이메일 형식이 아닙니다.")
  @NotBlank(message = "이메일은 필수입니다.")
  private String email;

  private String profileImageUrl;
}
