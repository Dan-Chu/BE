package com.likelion.danchu.domain.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.likelion.danchu.domain.auth.exception.AuthErrorCode;
import com.likelion.danchu.domain.user.dto.request.UserRequest;
import com.likelion.danchu.domain.user.dto.response.UserResponse;
import com.likelion.danchu.domain.user.entity.User;
import com.likelion.danchu.domain.user.mapper.UserMapper;
import com.likelion.danchu.domain.user.repository.UserRepository;
import com.likelion.danchu.domain.user.service.UserService;
import com.likelion.danchu.global.exception.CustomException;
import com.likelion.danchu.global.jwt.JwtProvider;
import com.likelion.danchu.global.redis.RedisUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;
  private final RedisUtil redisUtil;
  private final UserMapper userMapper;
  private final UserService userService;
  private static final String REFRESH_TOKEN_PREFIX = "user:refresh:";

  @Value("${cookie.secure}")
  private boolean secure;

  /**
   * 일반 로그인을 처리하는 메서드
   *
   * @param loginRequest 사용자 로그인 요청 객체 (이메일, 비밀번호 포함)
   * @param response 액세스 토큰과 리프레시 토큰을 담기 위한 HTTP 응답 객체
   * @return 로그인한 사용자 정보가 담긴 {@link UserResponse} 객체
   * @throws CustomException 이메일에 해당하는 사용자가 없을 경우 {@link AuthErrorCode#INVALID_PASSWORD}
   * @throws CustomException 비밀번호가 일치하지 않을 경우 {@link AuthErrorCode#INVALID_PASSWORD}
   */
  public UserResponse login(UserRequest.LoginRequest loginRequest, HttpServletResponse response) {
    User user = validateUserCredentials(loginRequest);
    return issueTokensAndSetResponse(user, response);
  }

  /**
   * 테스트용 로그인 계정(ID = 1)을 통해 로그인을 처리하는 메서드
   *
   * @param response 액세스 토큰과 리프레시 토큰을 담기 위한 HTTP 응답 객체
   * @return 로그인한 테스트 사용자 정보가 담긴 {@link UserResponse} 객체
   * @throws CustomException ID가 1인 테스트 사용자가 존재하지 않을 경우 {@link
   *     AuthErrorCode#AUTHENTICATION_NOT_FOUND}
   */
  public UserResponse testLogin(HttpServletResponse response) {
    User user =
        userRepository
            .findById(1L)
            .orElseThrow(() -> new CustomException(AuthErrorCode.AUTHENTICATION_NOT_FOUND));
    return issueTokensAndSetResponse(user, response);
  }

  // 사용자 인증 (이메일 + 비밀번호 검증)
  private User validateUserCredentials(UserRequest.LoginRequest loginRequest) {
    User user =
        userRepository
            .findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_PASSWORD));

    if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
      throw new CustomException(AuthErrorCode.INVALID_PASSWORD);
    }

    return user;
  }

  // 토큰 발급 및 응답 세팅
  private UserResponse issueTokensAndSetResponse(User user, HttpServletResponse response) {
    String accessToken = jwtProvider.createAccessToken(user.getId());
    String refreshToken = jwtProvider.createRefreshToken(user.getId());

    long refreshTokenExpireSeconds = jwtProvider.getRefreshTokenExpireTime() / 1000;
    redisUtil.setData(REFRESH_TOKEN_PREFIX + user.getId(), refreshToken, refreshTokenExpireSeconds);

    setAccessTokenHeader(response, accessToken);
    setRefreshTokenCookie(response, refreshToken, refreshTokenExpireSeconds);

    long completedMission = userService.getCompletedMission(user.getId());
    return userMapper.toResponse(user, completedMission);
  }

  private void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
    response.setHeader("Authorization", "Bearer " + accessToken);
  }

  private void setRefreshTokenCookie(
      HttpServletResponse response, String refreshToken, long maxAgeSeconds) {
    Cookie cookie = new Cookie("refreshToken", refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(secure);
    cookie.setPath("/");
    cookie.setMaxAge((int) maxAgeSeconds);
    response.addCookie(cookie);
  }
}
