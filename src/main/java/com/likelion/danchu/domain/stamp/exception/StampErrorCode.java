package com.likelion.danchu.domain.stamp.exception;

import org.springframework.http.HttpStatus;

import com.likelion.danchu.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StampErrorCode implements BaseErrorCode {
  EXAMPLE_ERROR_CODE("STAMP_0000", "예시 에러코드로 커스터마이징이 필요합니다.", HttpStatus.BAD_REQUEST),

  INVALID_AUTH_CODE("STAMP_0001", "올바르지 않은 인증코드입니다.", HttpStatus.BAD_REQUEST),
  ALREADY_READY_TO_CLAIM("STAMP_0002", "이미 스탬프 카드가 존재합니다. 보상을 수령해주세요.", HttpStatus.CONFLICT),
  INVALID_STAMP_STATUS("STAMP_0003", "유효하지 않은 스탬프 상태입니다.", HttpStatus.BAD_REQUEST),

  STAMP_SAVE_FAILED("STAMP_0004", "스탬프 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

  STAMP_FETCH_FAILED("STAMP_0006", "스탬프 조회에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}
