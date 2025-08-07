package com.likelion.danchu.domain.hashtag.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.danchu.domain.hashtag.dto.request.HashtagRequest;
import com.likelion.danchu.domain.hashtag.dto.response.HashtagResponse;
import com.likelion.danchu.domain.hashtag.service.HashtagService;
import com.likelion.danchu.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
@Tag(name = "Hashtag", description = "HashTag 관련 API")
public class HashtagController {

  private final HashtagService hashtagService;

  @Operation(
      summary = "가게 해시태그 등록",
      description =
          """
              특정 가게에 해시태그를 등록합니다.

              - 해시태그 이름은 **'#' 없이 입력해도 자동으로 붙여집니다**.
              - 영어는 **모두 소문자로 변환**되어 저장됩니다.
              - 이름은 **비어 있을 수 없으며**, 최소 1자 이상, 최대 10자 이하로 입력해야 합니다.
              """)
  @PostMapping("/stores/{storeId}/hashtags")
  public ResponseEntity<BaseResponse<HashtagResponse>> createHashtagForStore(
      @Parameter(name = "storeId", description = "가게 ID", example = "1", required = true)
          @PathVariable
          Long storeId,
      @Valid @RequestBody HashtagRequest hashtagRequest) {
    HashtagResponse hashtagResponse = hashtagService.createHashtagForStore(storeId, hashtagRequest);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success("해시태그 생성에 성공했습니다.", hashtagResponse));
  }

  @Operation(
      summary = "전체 해시태그 조회",
      description =
          """
              등록된 모든 해시태그를 조회하는 API입니다.

              응답은 저장된 순서(생성일 기준)로 정렬되어 제공됩니다.
              """)
  @GetMapping("/hashtags")
  public ResponseEntity<BaseResponse<List<HashtagResponse>>> getAllHashtags() {
    List<HashtagResponse> hashtags = hashtagService.getAllHashtags();
    return ResponseEntity.ok(BaseResponse.success("해시태그 목록 조회에 성공했습니다.", hashtags));
  }
}
