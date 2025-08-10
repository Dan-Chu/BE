package com.likelion.danchu.domain.mission.exception;

import org.springframework.http.HttpStatus;

import com.likelion.danchu.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MissionErrorCode implements BaseErrorCode {
  EXAMPLE_ERROR_CODE("MISSION_0000", "예시 에러코드로 커스터마이징이 필요합니다.", HttpStatus.BAD_REQUEST),

  STORE_NOT_FOUND("MISSION_0001", "해당 가게를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  REWARD_UPLOAD_FAILED("MISSION_0002", "보상 이미지 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  DUPLICATE_MISSION("MISSION_0003", "같은 가게에서 같은 날짜에 동일한 제목의 미션이 이미 존재합니다.", HttpStatus.CONFLICT),
  
  MISSION_NOT_FOUND("MISSION_0004", "해당 미션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);


  private final String code;
  private final String message;
  private final HttpStatus status;
}
