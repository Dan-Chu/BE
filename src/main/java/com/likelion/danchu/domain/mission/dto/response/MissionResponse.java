package com.likelion.danchu.domain.mission.dto.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "MissionResponse DTO", description = "미션 관련 응답")
public class MissionResponse {

  @Schema(description = "미션 ID", example = "1")
  private Long id;

  @Schema(description = "대상 가게 ID", example = "1")
  private Long storeId;

  @Schema(description = "미션 제목", example = "오픈 시간 방문 미션")
  private String title;

  @Schema(description = "미션 설명", example = "오전 10시~12시 사이 방문 시 무료 음료 제공")
  private String description;

  @Schema(description = "보상 정보", example = "사이다 1캔 무료")
  private String reward;

  @Schema(description = "미션 진행 날짜 (yyyy-MM-dd)", example = "2025-08-08")
  private LocalDate date;

  @Schema(
      description = "보상 이미지 URL",
      example = "https://s3.amazonaws.com/bucket/missions/reward.jpg")
  private String rewardImageUrl;
}
