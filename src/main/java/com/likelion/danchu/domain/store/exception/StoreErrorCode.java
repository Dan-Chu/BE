package com.likelion.danchu.domain.store.exception;

import org.springframework.http.HttpStatus;

import com.likelion.danchu.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StoreErrorCode implements BaseErrorCode {
  EXAMPLE_ERROR_CODE("STORE_0000", "예시 에러코드로 커스터마이징이 필요합니다.", HttpStatus.BAD_REQUEST),

  STORE_ALREADY_EXISTS("STORE_0001", "이미 등록된 가게입니다.", HttpStatus.CONFLICT),
  IMAGE_UPLOAD_FAILED("STORE_0002", "이미지 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  STORE_SAVE_FAILED("STORE_0003", "가게 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  STORE_AUTHCODE_DUPLICATED("STORE_0004", "이미 사용 중인 인증 코드입니다.", HttpStatus.CONFLICT);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
