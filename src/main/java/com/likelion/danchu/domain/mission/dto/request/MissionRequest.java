package com.likelion.danchu.domain.mission.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "MissionRequest DTO", description = "미션 관련 요청")
public class MissionRequest {

  @NotNull(message = "가게 ID는 필수입니다.")
  private Long storeId;

  @NotBlank(message = "미션 제목은 필수입니다.")
  private String title;

  @NotBlank(message = "미션 설명은 필수입니다.")
  private String description;

  @NotBlank(message = "보상 정보는 필수입니다.")
  private String reward;

  @NotNull(message = "미션 날짜는 필수입니다.")
  @FutureOrPresent(message = "미션 날짜는 오늘 이후여야 합니다.")
  private LocalDate date;

  private String rewardImageUrl;
}
