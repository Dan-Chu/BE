package com.likelion.danchu.domain.hashtag.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.danchu.domain.hashtag.dto.request.HashtagRequest;
import com.likelion.danchu.domain.hashtag.dto.response.HashtagResponse;
import com.likelion.danchu.domain.hashtag.service.HashtagService;
import com.likelion.danchu.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hashtags")
@Tag(name = "Hashtag", description = "HashTag 관련 API")
public class HashtagController {

  private final HashtagService hashtagService;

  @Operation(
      summary = "해시태그 등록",
      description =
          """
        새로운 해시태그를 추가할 때 사용하는 API입니다.

        - 해시태그 이름은 **'#' 없이 입력해도 자동으로 붙여집니다**.
        - 영어는 **모두 소문자로 변환**되어 저장됩니다.
        - 이름은 **비어 있을 수 없으며**, 최소 1자 이상, 최대 10자 이하로 입력해야 합니다.
        """)
  @PostMapping
  public ResponseEntity<BaseResponse<HashtagResponse>> createHashtag(
      @Valid @RequestBody HashtagRequest hashtagRequest) {
    HashtagResponse hashtagResponse = hashtagService.createHashtag(hashtagRequest);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success("해시태그 생성에 성공했습니다.", hashtagResponse));
  }
}
