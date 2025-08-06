package com.likelion.danchu.domain.user.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

  @Operation(summary = "회원가입", description = """
          새로운 사용자를 등록합니다.


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
}
