package com.likelion.danchu.domain.openAI.exception;

import org.springframework.http.HttpStatus;

import com.likelion.danchu.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OpenAIErrorCode implements BaseErrorCode {
  EXAMPLE_ERROR_CODE("OPENAI_0000", "예시 에러코드로 커스터마이징이 필요합니다.", HttpStatus.BAD_REQUEST),

  OPENAI_REQUEST_FAILED("OPENAI_0001", "OpenAI 요청 처리에 실패했습니다.", HttpStatus.BAD_GATEWAY),
  OPENAI_INVALID_RESPONSE("OPENAI_0002", "OpenAI 응답 포맷이 올바르지 않습니다.", HttpStatus.BAD_GATEWAY),
  USER_HASHTAG_EMPTY("OPENAI_0003", "사용자 해시태그가 없어 추천을 진행할 수 없습니다.", HttpStatus.BAD_REQUEST),

  OPENAI_RESPONSE_EMPTY("OPENAI_0004", "OpenAI 응답이 비어 있습니다.", HttpStatus.BAD_GATEWAY),
  OPENAI_RESPONSE_PARSE_FAIL("OPENAI_0005", "OpenAI 응답 파싱에 실패했습니다.", HttpStatus.BAD_GATEWAY),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}
