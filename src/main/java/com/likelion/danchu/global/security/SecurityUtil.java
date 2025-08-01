package com.likelion.danchu.global.security;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

  private static final Long TEST_USER_ID = 1L;

  public static Long getCurrentUserId() {
    return TEST_USER_ID;
  }

  public static boolean isTestUser() {
    return true;
  }
}
