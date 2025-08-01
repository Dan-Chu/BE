package com.likelion.danchu.domain.hashtag.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "HashtagResponse DTO", description = "해시태그 관련 응답")
public class HashtagResponse {}
