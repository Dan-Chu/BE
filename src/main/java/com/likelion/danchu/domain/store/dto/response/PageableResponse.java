package com.likelion.danchu.domain.store.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "Pageable 응답 DTO", description = "페이징이 적용된 응답 형식")
public class PageableResponse<T> {

  @Schema(description = "현재 페이지 데이터 리스트")
  private List<T> content;

  @Schema(description = "현재 페이지 번호", example = "0")
  private int currentPage;

  @Schema(description = "전체 페이지 수", example = "5")
  private int totalPages;

  @Schema(description = "전체 요소 수", example = "15")
  private long totalElements;

  @Schema(description = "첫 페이지 여부", example = "true")
  private boolean isFirst;

  @Schema(description = "마지막 페이지 여부", example = "false")
  private boolean isLast;

  @Schema(description = "페이지 당 데이터 수", example = "3")
  private int pageSize;

  @Schema(description = "현재 페이지의 요소 수", example = "3")
  private int numberOfElements;

  public static <T> PageableResponse<T> from(Page<T> page) {
    return PageableResponse.<T>builder()
        .content(page.getContent())
        .currentPage(page.getNumber())
        .totalPages(page.getTotalPages())
        .totalElements(page.getTotalElements())
        .isFirst(page.isFirst())
        .isLast(page.isLast())
        .pageSize(page.getSize())
        .numberOfElements(page.getNumberOfElements())
        .build();
  }
}
