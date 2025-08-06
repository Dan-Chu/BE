package com.likelion.danchu.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "UserResponse DTO", description = "사용자 관련 응답")
public class UserResponse {

  @Schema(description = "사용자 ID", example = "1")
  private Long id;

  @Schema(description = "사용자 닉네임", example = "danchu")
  private String nickname;

  @Schema(description = "사용자 이메일", example = "danchu@skuniv.ac.kr")
  private String email;

  @Schema(description = "완료 미션 개수", example = "7")
  private long completedMission;

  @Schema(
      description = "프로필 이미지 URL (null 가능)",
      example = "https://danchu.s3.ap-northeast-2.amazonaws.com/bucket/image.jpg",
      nullable = true)
  private String profileImageUrl;
}
