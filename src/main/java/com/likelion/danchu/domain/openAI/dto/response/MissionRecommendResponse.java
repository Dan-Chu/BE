package com.likelion.danchu.domain.openAI.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "MissionRecommendResponse DTO", description = "맞춤형 미션 추천 응답")
public class MissionRecommendResponse {

  @Schema(description = "미션 ID", example = "1")
  private Long missionId;

  @Schema(description = "미션 제목", example = "점심시간 방문 인증")
  private String title;

  @Schema(description = "미션 보상(이름/설명)", example = "사이다 1잔")
  private String rewardName;

  @Schema(description = "가게 이름", example = "동방손칼국수")
  private String storeName;
}
