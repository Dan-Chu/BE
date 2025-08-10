package com.likelion.danchu.domain.coupon.exception;

import org.springframework.http.HttpStatus;

import com.likelion.danchu.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CouponErrorCode implements BaseErrorCode {
  EXAMPLE_ERROR_CODE("COUPON_0000", "예시 에러코드로 커스터마이징이 필요합니다.", HttpStatus.BAD_REQUEST),

  COUPON_NOT_FOUND("COUPON_0001", "해당 쿠폰을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  INVALID_REWARD("COUPON_0002", "쿠폰 리워드가 비어있습니다.", HttpStatus.BAD_REQUEST),
  IMAGE_UPLOAD_FAILED("COUPON_0003", "쿠폰 이미지 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  COUPON_SAVE_FAILED("COUPON_0004", "쿠폰 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  IMAGE_REQUIRED("COUPON_0005", "쿠폰 이미지는 필수입니다.", HttpStatus.BAD_REQUEST),

  COUPON_FETCH_FAILED("COUPON_0006", "쿠폰 조회에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

  INVALID_AUTH_CODE("COUPON_0007", "올바르지 않은 인증코드입니다.", HttpStatus.BAD_REQUEST),
  COUPON_STORE_MISMATCH("COUPON_0008", "쿠폰의 가게와 인증코드가 일치하지 않습니다.", HttpStatus.FORBIDDEN),
  COUPON_OWNER_MISMATCH("COUPON_0009", "본인의 쿠폰만 사용할 수 있습니다.", HttpStatus.FORBIDDEN),
  COUPON_DELETE_FAILED("COUPON_0010", "쿠폰 사용 처리(삭제)에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}
