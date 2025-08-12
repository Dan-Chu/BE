package com.likelion.danchu.domain.menu.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "MenuRequest DTO", description = "메뉴 관련 요청")
public class MenuRequest {

  @NotBlank(message = "메뉴 이름은 필수입니다.")
  @Size(max = 50, message = "메뉴 이름은 50자 이내여야 합니다.")
  @Schema(description = "메뉴 이름", example = "치즈돈까스")
  private String name;

  @NotNull(message = "메뉴 가격은 필수입니다.")
  @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
  @Schema(description = "메뉴 가격", example = "9000")
  private Integer price;
}
