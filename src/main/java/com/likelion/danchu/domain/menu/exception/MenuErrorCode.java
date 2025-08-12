package com.likelion.danchu.domain.menu.exception;

import org.springframework.http.HttpStatus;

import com.likelion.danchu.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MenuErrorCode implements BaseErrorCode {
  MENU_NOT_FOUND("MENU_0001", "메뉴를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  DUPLICATE_MENU_NAME("MENU_0002", "해당 가게에 동일한 메뉴명이 이미 존재합니다.", HttpStatus.CONFLICT),
  MENU_UPLOAD_FAILED("MENU_0003", "메뉴 이미지 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
