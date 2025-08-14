package com.likelion.danchu.domain.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.likelion.danchu.domain.auth.service.AuthService;
import com.likelion.danchu.domain.user.dto.request.UserRequest;
import com.likelion.danchu.domain.user.dto.response.UserResponse;
import com.likelion.danchu.domain.user.service.UserService;
import com.likelion.danchu.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User", description = "User 관련 API")
public class UserController {
  private final UserService userService;
  private final AuthService authService;

  @Operation(
      summary = "회원가입",
      description =
          """
      새로운 사용자를 등록합니다.

      ✅ 요청 형식:
      - **userRequest**: 사용자 정보(JSON) \s
      - **image**: 프로필 이미지 파일 (선택)
          """)
  @PostMapping(
      path = "/register",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BaseResponse<UserResponse>> registerUser(
      @Parameter(
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE),
              description = "회원 정보(JSON)")
          @RequestPart("userRequest")
          @Valid
          UserRequest.InfoRequest userRequest,
      @Parameter(
              content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE),
              description = "프로필 이미지 파일(선택)")
          @RequestPart(value = "image", required = false)
          MultipartFile imageFile) {

    UserResponse userResponse = userService.register(userRequest, imageFile);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success("회원 가입이 완료되었습니다.", userResponse));
  }

  @Operation(
      summary = "완료 미션 개수 조회",
      description =
          """
        로그인한 사용자의 완료한 미션 개수를 조회합니다.

        ✅ 인증 필요: \s
        - 이 API는 **JWT 인증이 필요한 API**입니다. \s
        - 헤더에 **Authorization: Bearer <AccessToken>**을 포함해주세요.

        ✅ 동작 방식: \s
        - Redis에 캐시된 값이 있으면 해당 값을 반환합니다. \s
        - 없을 경우 DB에서 값을 조회한 후 Redis에 저장하고 응답합니다.
        """)
  @GetMapping("/missions/count")
  public ResponseEntity<BaseResponse<Long>> getCompletedMissionCount() {
    long count = userService.getCompletedMissionCountForCurrentUser();
    return ResponseEntity.ok(BaseResponse.success("완료 미션 개수를 조회했습니다.", count));
  }

  @Operation(
      summary = "회원 정보 수정",
      description =
          """
          회원의 정보를 수정합니다.
          (닉네임, 이메일, 프로필 이미지, 관심 해시태그 포함)

          - 닉네임: **2자 이상, 20자 이내**
          - 이메일: **이메일 형식 필수 (예: danchu@skuniv.ac.kr)**
          - 관심 해시태그 목록: **해시태그 목록에 있는 문자열 리스트 (예: '매운맛', '단짠단짠')**
          - 프로필 이미지: **multipart/form-data** 형식의 파일 (null: 기존 이미지 유지)
          - 이메일/닉네임 중복 시 에러가 발생합니다.
          """)
  @PutMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseResponse<UserResponse>> updateUserInfo(
      @RequestPart("userRequest") @Valid UserRequest.UpdateRequest request,
      @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
          @RequestPart(value = "imageFile", required = false)
          MultipartFile imageFile) {

    UserResponse response = userService.updateUser(request, imageFile);
    return ResponseEntity.ok(BaseResponse.success("회원 정보 수정 성공", response));
  }

  @Operation(
      summary = "회원 정보 조회",
      description =
          """
          현재 로그인한 사용자의 프로필 정보를 조회합니다.

          - 반환 항목:
            - 사용자 기본 정보: **id, nickname, email**
            - 완료 미션 개수: **completedMission**
            - 프로필 이미지: **profileImageUrl** (null 가능)
            - 관심 해시태그: **hashtags 목록** (없으면 빈 배열)
          """)
  @GetMapping("")
  public ResponseEntity<BaseResponse<UserResponse>> getUserInfo() {

    UserResponse response = userService.getUserInfo();
    return ResponseEntity.ok(BaseResponse.success("회원 정보 조회 성공", response));
  }

  @Operation(
      summary = "회원 탈퇴",
      description =
          """
      현재 로그인한 사용자를 **영구 삭제**합니다.

      인증
      - JWT 필수: **Authorization: Bearer <AccessToken>**

      삭제/정리 범위
      - S3: 사용자 프로필 이미지, 사용자가 보유한 쿠폰 이미지
      - DB: userHashtag, stamp(스탬프카드), coupon(쿠폰), user(사용자)
      - Redis: refreshToken, completedMission 캐시
      - 토큰 무효화: Access Token 블랙리스트 등록, refreshToken 쿠키/Redis 삭제(즉시 로그아웃)

      주의
      - 삭제 후 복구할 수 없습니다.
      - 처리 직후부터 기존 토큰으로의 인증이 필요한 모든 API 호출은 차단됩니다.
      """)
  @DeleteMapping("")
  public ResponseEntity<BaseResponse<String>> deleteMe(
      HttpServletRequest request, HttpServletResponse response) {

    userService.deleteCurrentUser();
    authService.invalidateCurrentSessionQuietly(request, response);
    return ResponseEntity.ok(BaseResponse.success("회원 탈퇴가 완료되었습니다."));
  }
}
