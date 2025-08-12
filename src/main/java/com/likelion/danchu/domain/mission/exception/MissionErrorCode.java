package com.likelion.danchu.domain.mission.exception;

import com.likelion.danchu.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MissionErrorCode implements BaseErrorCode {
  EXAMPLE_ERROR_CODE("MISSION_0000", "예시 에러코드로 커스터마이징이 필요합니다.", HttpStatus.BAD_REQUEST),

  STORE_NOT_FOUND("MISSION_0001", "해당 가게를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  REWARD_UPLOAD_FAILED("MISSION_0002", "보상 이미지 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  DUPLICATE_MISSION("MISSION_0003", "같은 가게에서 같은 날짜에 동일한 제목의 미션이 이미 존재합니다.", HttpStatus.CONFLICT),
  MISSION_NOT_FOUND("MISSION_0004", "해당 미션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  INVALID_AUTH_CODE("MISSION_0005", "올바르지 않은 인증코드입니다.", HttpStatus.BAD_REQUEST),
  MISSION_STORE_MISMATCH("MISSION_0006", "미션의 가게와 인증코드가 일치하지 않습니다.", HttpStatus.FORBIDDEN),
  ALREADY_COMPLETED("MISSION_0007", "이미 완료한 미션입니다.", HttpStatus.CONFLICT),
  POPULAR_MISSION_NOT_FOUND("MISSION_0008", "완료된 미션 기록이 없습니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
