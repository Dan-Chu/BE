package com.likelion.danchu.domain.menu.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "MenuResponse DTO", description = "메뉴 관련 응답")
public class MenuResponse {

  @Schema(description = "메뉴 ID", example = "1")
  private Long id;

  @Schema(description = "가게 ID", example = "10")
  private Long storeId;

  @Schema(description = "가게 이름", example = "동방손칼국수")
  private String storeName;

  @Schema(description = "메뉴 이름", example = "치즈돈까스")
  private String name;

  @Schema(description = "메뉴 가격(원, 숫자)", example = "9000")
  private Integer price;

  @Schema(description = "표시용 가격 문자열", example = "9,000원")
  private String priceFormatted;

  @Schema(description = "이미지 URL", example = "https://s3.amazonaws.com/bucket/menus/menu.jpg")
  private String imageUrl;
}
