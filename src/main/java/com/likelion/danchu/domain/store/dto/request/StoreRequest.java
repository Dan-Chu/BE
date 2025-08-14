package com.likelion.danchu.domain.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "StoreRequest DTO", description = "가게 관련 요청")
@JsonPropertyOrder({
  "name",
  "address",
  "description",
  "phoneNumber",
  "openTime",
  "closeTime",
  "authCode",
  "stampReward"
})
public class StoreRequest {

  @NotBlank(message = "가게 이름은 필수입니다.")
  @Size(max = 10, message = "가게 이름은 10자 이내여야 합니다.")
  @Schema(description = "가게 이름", example = "동방손칼국수")
  private String name;

  @NotBlank(message = "주소는 필수입니다.")
  @Size(max = 50, message = "주소는 50자 이내여야 합니다.")
  @Schema(description = "가게 주소", example = "서울 성북구 서경로 91")
  private String address;

  @NotBlank(message = "가게 소개는 필수입니다.")
  @Size(max = 200, message = "가게 소개는 200자 이내여야 합니다.")
  @Schema(description = "가게 소개", example = "묵은지의 깊은 맛이 담긴 닭볶음탕")
  private String description;

  @NotBlank(message = "전화번호는 필수입니다.")
  @Pattern(
      regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$",
      message = "전화번호는 형식에 맞게 입력해주세요. (예: 010-1234-5678 / 02-345-6789)")
  @Schema(description = "전화번호", example = "010-1234-5678")
  private String phoneNumber;

  @NotBlank(message = "오픈 시간은 필수입니다.")
  @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "오픈 시간 형식이 올바르지 않습니다. 예: 09:00")
  @Schema(description = "오픈 시간 (HH:mm 형식)", example = "09:00")
  private String openTime;

  @NotBlank(message = "마감 시간은 필수입니다.")
  @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "마감 시간 형식이 올바르지 않습니다. 예: 21:00")
  @Schema(description = "마감 시간 (HH:mm 형식)", example = "21:00")
  private String closeTime;

  @NotBlank(message = "인증 코드는 필수입니다.")
  @Pattern(regexp = "^\\d{4}$", message = "인증 코드는 숫자 4자리여야 합니다.")
  @Schema(description = "가게 인증 코드 (숫자 4자리)", example = "0123")
  private String authCode;

  @Schema(description = "스탬프카드 적립 보상", example = "무료 아메리카노 1잔")
  private String stampReward;
}
