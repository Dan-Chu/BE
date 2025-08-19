package com.likelion.danchu.domain.stamp.dto.response;

import com.likelion.danchu.domain.stamp.entity.StampStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "StampResponse DTO", description = "스탬프 관련 응답")
public class StampResponse {

  @Schema(description = "스탬프 ID", example = "1")
  private Long id;

  @Schema(description = "가게 이름", example = "르아브르")
  private String storeName;

  @Schema(description = "보상 내용", example = "아메리카노 1잔 무료")
  private String reward;

  @Schema(description = "현재 스탬프카드의 스탬프 개수", example = "6")
  private int currentCount;

  @Schema(description = "누적 스탬프카드 개수", example = "1")
  private int cardNum;

  @Schema(description = "스탬프카드 상태 (적립 진행중 or 보상 수령 대기)", example = "IN_PROGRESS")
  private StampStatus status;

  @Schema(description = "사용자 이름", example = "danchu")
  private String nickname;

  @Schema(description = "가게 인증코드", example = "0000")
  private String authCode;
}
