package com.likelion.danchu.domain.hashtag.exception;

import org.springframework.http.HttpStatus;

import com.likelion.danchu.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HashtagErrorCode implements BaseErrorCode {
  EXAMPLE_ERROR_CODE("HASHTAG_0000", "예시 에러코드로 커스터마이징이 필요합니다.", HttpStatus.BAD_REQUEST),

  HASHTAG_ALREADY_EXISTS("HASHTAG_0001", "이미 존재하는 해시태그입니다.", HttpStatus.CONFLICT),
  HASHTAG_LENGTH_INVALID("HASHTAG_0002", "해시태그는 1자 이상 10자 이하로 입력해야 합니다.", HttpStatus.BAD_REQUEST),

  HASHTAG_NOT_FOUND("HASHTAG_0004", "존재하지 않는 해시태그입니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
