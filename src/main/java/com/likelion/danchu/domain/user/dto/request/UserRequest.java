package com.likelion.danchu.domain.user.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.likelion.danchu.domain.hashtag.dto.request.HashtagRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(title = "UserRequest DTO", description = "사용자 관련 요청 컨테이터")
public class UserRequest {

  @Getter
  @AllArgsConstructor
  @Schema(name = "InfoRequest", description = "회원 정보 요청")
  public static class InfoRequest {

    @NotBlank(message = "사용자 닉네임 항목은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다.")
    @Pattern(regexp = "^[가-힣A-Za-z0-9]+$", message = "닉네임은 한글, 영문, 숫자만 가능합니다.")
    @Schema(description = "사용자 닉네임", example = "danchu", maxLength = 20)
    private String nickname;

    @NotBlank(message = "사용자 이메일 항목은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 50, message = "이메일은 50자 이하여야 합니다.")
    @Schema(description = "사용자 이메일", example = "danchu@skuniv.ac.kr", maxLength = 50)
    private String email;

    @NotBlank(message = "비밀번호 항목은 필수입니다.")
    @Size(max = 20, message = "비밀번호는 20자 이하로 입력해주세요.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,}$",
        message = "비밀번호는 최소 8자 이상, 영문·숫자·특수문자를 포함해야 합니다.")
    @Schema(description = "비밀번호", example = "qwer1234!", maxLength = 20)
    private String password;
  }

  @Getter
  @Schema(name = "LoginRequest", description = "로그인 요청")
  public static class LoginRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Schema(description = "사용자 이메일", example = "danchu@skuniv.ac.kr")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(description = "사용자 비밀번호", example = "qwer1234!")
    private String password;
  }

  @Getter
  @Schema(name = "UpdateRequest", description = "회원 수정 요청")
  public static class UpdateRequest {
    @NotBlank(message = "사용자 닉네임 항목은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다.")
    @Pattern(regexp = "^[가-힣A-Za-z0-9]+$", message = "닉네임은 한글, 영문, 숫자만 가능합니다.")
    @Schema(description = "사용자 닉네임", example = "danchu", maxLength = 20)
    private String nickname;

    @NotBlank(message = "사용자 이메일 항목은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 50, message = "이메일은 50자 이하여야 합니다.")
    @Schema(description = "사용자 이메일", example = "danchu@skuniv.ac.kr", maxLength = 50)
    private String email;

    @Schema(
        description =
            """
        관심 해시태그 목록입니다.

        - 해시태그 이름만 전달하면 자동으로 '#'이 붙고 소문자로 변환됩니다.
        - 각 해시태그는 1자 이상, 10자 이하입니다.
        - 중복된 해시태그는 제거됩니다.
        """,
        example =
            """
        [
          {"name": "매운맛"},
          {"name": "단짠단짠"}
        ]
        """)
    private List<@Valid HashtagRequest> hashtags;
  }
}
