package com.likelion.danchu.domain.store.dto.response;

import java.util.List;

import com.likelion.danchu.domain.hashtag.dto.response.HashtagResponse;
import com.likelion.danchu.domain.menu.dto.response.MenuResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "StoreResponse DTO", description = "가게 관련 응답")
public class StoreResponse {

  @Schema(description = "가게 ID", example = "1")
  private Long id;

  @Schema(description = "가게 이름", example = "동방손칼국수")
  private String name;

  @Schema(description = "가게 주소", example = "서울 성북구 서경로 91 청구아파트제상가동 1층 105호")
  private String address;

  @Schema(description = "가게 설명", example = "묵은지의 깊은 맛이 담긴 닭볶음탕")
  private String description;

  @Schema(description = "전화번호", example = "010-1234-5678")
  private String phoneNumber;

  @Schema(description = "오픈 시간 (HH:mm)", example = "09:00")
  private String openTime;

  @Schema(description = "마감 시간 (HH:mm)", example = "21:00")
  private String closeTime;

  @Schema(description = "대표 이미지 URL", example = "https://s3.amazonaws.com/bucket/image.jpg")
  private String mainImageUrl;

  @Schema(description = "가게 해시태그 목록")
  private List<HashtagResponse> hashtags;

  @Schema(description = "현재 영업 여부", example = "true")
  private boolean isOpen;

  @Schema(description = "가게 메뉴 목록")
  private List<MenuResponse> menus;
}
