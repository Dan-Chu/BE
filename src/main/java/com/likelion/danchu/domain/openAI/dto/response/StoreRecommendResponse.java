package com.likelion.danchu.domain.openAI.dto.response;

import java.util.List;

import com.likelion.danchu.domain.hashtag.dto.response.HashtagResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "StoreRecommendResponse DTO", description = "맞춤형 가게 추천 관련 응답")
public class StoreRecommendResponse {

  @Schema(description = "가게 ID", example = "1")
  private Long id;

  @Schema(description = "가게 이름", example = "동방손칼국수")
  private String name;

  @Schema(description = "대표 이미지 URL", example = "https://s3.amazonaws.com/bucket/image.jpg")
  private String mainImageUrl;

  @Schema(description = "가게 해시태그 목록")
  private List<HashtagResponse> hashtags;
}
