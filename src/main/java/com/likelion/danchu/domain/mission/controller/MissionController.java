package com.likelion.danchu.domain.mission.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.likelion.danchu.domain.hashtag.service.HashtagService;
import com.likelion.danchu.domain.mission.dto.request.MissionRequest;
import com.likelion.danchu.domain.mission.dto.response.MissionResponse;
import com.likelion.danchu.domain.mission.service.MissionService;
import com.likelion.danchu.global.response.BaseResponse;
import com.likelion.danchu.global.security.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/missions")
@Tag(name = "Mission", description = "Mission 관련 API")
public class MissionController {

  private final HashtagService hashtagService;
  private final MissionService missionService;

  @Operation(
      summary = "미션 생성",
      description =
          """
              새로운 미션을 등록합니다.
              - date: **yyyy-MM-dd**
              - image: 선택
              """)
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BaseResponse<MissionResponse>> createMission(
      @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
          @RequestPart("missionRequest")
          @Valid
          MissionRequest missionRequest,
      @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
          @RequestPart(value = "image", required = false)
          MultipartFile imageFile) {

    MissionResponse missionResponse = missionService.createMission(missionRequest, imageFile);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success("미션 생성에 성공했습니다.", missionResponse));
  }

  @Operation(
      summary = "오늘의 미션 조회 (이미 완료한 미션 제외)",
      description =
          """
              - 오늘 날짜의 일일 미션만 반환합니다.
              - 로그인 사용자가 **이미 완료한 미션은 제외**됩니다.
              """)
  @GetMapping("/today")
  public ResponseEntity<BaseResponse<List<MissionResponse>>> getTodayMissions() {
    Long userId = SecurityUtil.getCurrentUserId();
    List<MissionResponse> responses = missionService.getTodayMissions(userId);
    return ResponseEntity.ok(BaseResponse.success("오늘의 미션 조회 성공", responses));
  }

  @Operation(summary = "미션 상세 조회", description = "미션 상세 내용을 조회합니다.")
  @GetMapping("/{missionId}")
  public ResponseEntity<BaseResponse<MissionResponse>> getMission(@PathVariable Long missionId) {
    MissionResponse response = missionService.getMission(missionId);
    return ResponseEntity.ok(BaseResponse.success("미션 조회에 성공했습니다.", response));
  }
}
