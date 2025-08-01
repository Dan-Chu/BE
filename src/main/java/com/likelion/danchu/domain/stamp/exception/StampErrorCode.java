package com.likelion.danchu.domain.stamp.exception;

import org.springframework.http.HttpStatus;

import com.likelion.danchu.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StampErrorCode implements BaseErrorCode {
  EXAMPLE_ERROR_CODE("STAMP_0000", "예시 에러코드로 커스터마이징이 필요합니다.", HttpStatus.BAD_REQUEST),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}
