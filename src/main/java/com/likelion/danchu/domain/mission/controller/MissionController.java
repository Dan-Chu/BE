package com.likelion.danchu.domain.mission.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.likelion.danchu.domain.coupon.dto.response.CouponResponse;
import com.likelion.danchu.domain.hashtag.service.HashtagService;
import com.likelion.danchu.domain.mission.dto.request.MissionRequest;
import com.likelion.danchu.domain.mission.dto.response.MissionResponse;
import com.likelion.danchu.domain.mission.service.MissionService;
import com.likelion.danchu.domain.stamp.dto.request.StampRequest;
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
  @PostMapping(
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BaseResponse<MissionResponse>> createMission(
      @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
          @RequestPart("missionRequest")
          @Valid
          MissionRequest.CreateRequest missionRequest,
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

  @Operation(
      summary = "미션 인증번호 입력 (미션 완료 인증)",
      description =
          """
              가게 인증코드를 입력하여 미션을 완료 처리하고, 보상 쿠폰을 **쿠폰함**으로 지급합니다. (로그인 필요)

              처리 순서:
              1) missionId로 미션 조회
              2) authCode로 가게 조회
              3) 미션의 가게와 인증코드의 가게 일치 검증
              4) 검증 성공 시 보상 쿠폰 발급
              """)
  @PostMapping(
      path = "/{missionId}/complete",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BaseResponse<CouponResponse>> completeMission(
      @PathVariable Long missionId, @Valid @RequestBody StampRequest stampRequest) {
    CouponResponse response =
        missionService.completeMissionWithAuthCode(missionId, stampRequest.getAuthCode());
    return ResponseEntity.ok(BaseResponse.success("미션 완료! 쿠폰함에 성공적으로 추가되었습니다.", response));
  }

  @Operation(
      summary = "오늘의 인기 미션 조회",
      description = "오늘 날짜의 미션 중에서 완료한 유저 수 기준으로 가장 인기 있는 미션 1건을 반환합니다.")
  @GetMapping("/popular")
  public ResponseEntity<BaseResponse<MissionResponse>> getPopularMission() {
    MissionResponse missionResponse = missionService.getPopularMission();
    return ResponseEntity.ok(BaseResponse.success("인기 미션 조회에 성공했습니다.", missionResponse));
  }

  @Operation(
      summary = "미션 날짜 변경",
      description =
          """
              특정 미션의 진행 날짜를 변경합니다.
              - 날짜 형식: **yyyy-MM-dd**
              - 오늘 또는 미래 날짜만 허용
              - 같은 가게/제목 조합에서 동일 날짜가 이미 존재하면 409(CONFLICT)
              """)
  @PutMapping(
      path = "/date",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BaseResponse<MissionResponse>> updateMissionDate(
      @Valid @RequestBody MissionRequest.DateUpdateRequest request) {

    MissionResponse response = missionService.updateMissionDate(request);
    return ResponseEntity.ok(BaseResponse.success("미션 날짜 변경에 성공했습니다.", response));
  }

  @Operation(
      summary = "미션 복제 생성",
      description =
          """
          완료 이력으로 날짜 변경이 불가할 때, 동일한 내용으로 다른 날짜에 새 미션을 생성합니다.
          - 날짜 형식: **yyyy-MM-dd**
          - 오늘 또는 미래 날짜만 허용
          - 같은 가게/제목/날짜가 이미 있으면 409(CONFLICT)
          """)
  @PostMapping(
      path = "/{missionId}/clone",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<BaseResponse<MissionResponse>> cloneMission(
      @PathVariable Long missionId, @Valid @RequestBody MissionRequest.CloneRequest request) {
    MissionResponse response = missionService.cloneMission(missionId, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success("미션 복제 생성에 성공했습니다.", response));
  }
}
