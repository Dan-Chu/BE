package com.likelion.danchu.domain.menu.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.likelion.danchu.domain.menu.dto.request.MenuRequest;
import com.likelion.danchu.domain.menu.dto.response.MenuResponse;
import com.likelion.danchu.domain.menu.service.MenuService;
import com.likelion.danchu.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores/{storeId}/menu")
@Tag(name = "Menu", description = "Menu 관련 API")
public class MenuController {

  private final MenuService menuService;

  @Operation(
      summary = "메뉴 생성",
      description =
          """
              가게에 메뉴를 등록합니다.
              - 메뉴명은 가게 내에서 중복될 수 없습니다.
              - 이미지 업로드는 선택사항입니다. (multipart/form-data)
              """)
  @PostMapping(
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BaseResponse<MenuResponse>> createMenu(
      @PathVariable Long storeId,
      @Parameter(
              description = "메뉴 생성 본문(JSON)",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
          @RequestPart("request")
          @Valid
          MenuRequest request,
      @Parameter(
              description = "메뉴 이미지 (선택)",
              content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
          @RequestPart(value = "imageFile", required = false)
          MultipartFile imageFile) {
    MenuResponse menuResponse = menuService.createMenu(storeId, request, imageFile);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success("메뉴 생성이 완료되었습니다.", menuResponse));
  }

  @Operation(summary = "메뉴 전체 조회", description = "생성한 가게의 모든 메뉴를 조회합니다.")
  @GetMapping
  public ResponseEntity<BaseResponse<List<MenuResponse>>> getMenus(@PathVariable Long storeId) {
    List<MenuResponse> responses = menuService.getMenus(storeId);
    return ResponseEntity.ok(BaseResponse.success("메뉴 전체 조회에 성공했습니다.", responses));
  }

  @Operation(summary = "특정 메뉴 삭제", description = "storeId와 menuId로 특정 메뉴를 삭제합니다.")
  @DeleteMapping("/{menuId}")
  public ResponseEntity<BaseResponse> deleteMenu(
      @Parameter(description = "가게 ID", example = "1") @PathVariable Long storeId,
      @Parameter(description = "메뉴 ID", example = "3") @PathVariable Long menuId) {
    menuService.deleteMenu(storeId, menuId);
    return ResponseEntity.ok(BaseResponse.success("메뉴 삭제에 성공했습니다."));
  }
}
