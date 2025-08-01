package com.likelion.danchu.domain.store.dto.request;

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
@Schema(title = "StoreRequest DTO", description = "가게 관련 요청")
public class StoreRequest {

  @NotBlank(message = "가게 이름은 필수입니다.")
  private String name;

  @NotBlank(message = "주소는 필수입니다.")
  private String address;

  @NotBlank(message = "가게 소개는 필수입니다.")
  private String description;

  @NotBlank(message = "전화번호는 필수입니다.")
  private String phoneNumber;

  @NotBlank(message = "영업 시간은 필수입니다.")
  private String openingHour;

  @NotBlank(message = "인증 코드는 필수입니다.")
  private String authCode;

  @NotBlank(message = "대표 이미지 URL은 필수입니다.")
  private String mainImageUrl;

  @NotBlank(message = "메뉴 이미지 URL은 필수입니다.")
  private String menuImageUrl;
}
