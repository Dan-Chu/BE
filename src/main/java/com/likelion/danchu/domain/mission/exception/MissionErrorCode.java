package com.likelion.danchu.domain.mission.exception;

import org.springframework.http.HttpStatus;

import com.likelion.danchu.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MissionErrorCode implements BaseErrorCode {
  EXAMPLE_ERROR_CODE("MISSION_0000", "예시 에러코드로 커스터마이징이 필요합니다.", HttpStatus.BAD_REQUEST),

  MISSION_NOT_FOUND("MISSION_0001", "해당 미션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}
