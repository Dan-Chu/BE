package com.likelion.danchu.domain.mission.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(title = "MissionRequest DTO", description = "미션 관련 요청")
public class MissionRequest {

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(name = "CreateRequest", description = "미션 생성 요청")
  public static class CreateRequest {

    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1")
    private Long storeId;

    @NotBlank(message = "미션 제목은 필수입니다.")
    @Size(max = 50, message = "미션 제목은 50자 이내여야 합니다.")
    @Schema(description = "미션 제목", example = "오픈 시간 방문 미션")
    private String title;

    @NotBlank(message = "미션 설명은 필수입니다.")
    @Size(max = 100, message = "미션 설명은 100자 이내여야 합니다.")
    @Schema(description = "미션 설명", example = "오전 10시~12시 사이 방문 시 무료 음료 제공")
    private String description;

    @NotBlank(message = "보상 정보는 필수입니다.")
    @Size(max = 100, message = "보상 정보는 100자 이내여야 합니다.")
    @Schema(description = "보상 정보", example = "사이다 1캔 무료")
    private String reward;

    @NotNull(message = "미션 날짜는 필수입니다.")
    @FutureOrPresent(message = "미션 날짜는 오늘 이후여야 합니다.")
    @Schema(description = "미션 진행 날짜 (yyyy-MM-dd)", example = "2025-08-08")
    private LocalDate date;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(name = "DateUpdateRequest", description = "미션 날짜 변경 요청")
  public static class DateUpdateRequest {

    @NotNull(message = "미션 ID는 필수입니다.")
    @Schema(description = "변경 대상 미션 ID", example = "1")
    private Long missionId;

    @NotNull(message = "변경할 날짜는 필수입니다.")
    @FutureOrPresent(message = "미션 날짜는 오늘 이후여야 합니다.")
    @Schema(description = "변경할 날짜 (yyyy-MM-dd)", example = "2025-08-20")
    private LocalDate date;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(name = "CloneRequest", description = "미션 복제 생성 요청(새 날짜)")
  public static class CloneRequest {

    @NotNull(message = "새 날짜는 필수입니다.")
    @FutureOrPresent(message = "미션 날짜는 오늘 이후여야 합니다.")
    @Schema(description = "복제 생성할 날짜 (yyyy-MM-dd)", example = "2025-08-21")
    private LocalDate date;
  }
}
