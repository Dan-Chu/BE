package com.likelion.danchu.domain.stamp.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.danchu.domain.stamp.dto.request.StampRequest;
import com.likelion.danchu.domain.stamp.dto.response.StampResponse;
import com.likelion.danchu.domain.stamp.service.StampService;
import com.likelion.danchu.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stamps")
@Tag(name = "Stamp", description = "Stamp 관련 API")
public class StampController {

  private final StampService stampService;

  @Operation(
      summary = "스탬프 적립 또는 새 카드 생성",
      description =
          """
          사용자가 매장에서 발급받은 인증코드로 스탬프를 적립합니다. (로그인 필요)

          **동작 시나리오**
          1. 인증코드가 유효하면 해당 매장을 찾습니다.
          2. 해당 매장의 스탬프카드가 아직 없으면 → 새 카드 생성 후 1개 적립된 상태로 반환합니다.
          3. 이미 스탬프카드가 있으면:
             - 카드가 **보상 수령 대기 상태**이면 → 적립 불가, '보상을 먼저 수령' 안내 에러 반환
             - 카드가 **진행 중 상태**이면 → 스탬프 1개 적립
               - 총 개수가 10개가 되면 상태를 **보상 수령 대기**로 변경

          **응답 데이터**
          - **currentCount**: 현재 카드에 적립된 개수 (count%10)
          - **cardNum**: 현재까지 완성한 스탬프카드의 개수 (count/10)
          - **status**: 카드 상태 (IN_PROGRESS / READY_TO_CLAIM)
          """)
  @PostMapping("")
  public ResponseEntity<BaseResponse<StampResponse>> createOrAccumulateStamp(
      @Valid @RequestBody StampRequest request) {
    StampResponse response = stampService.createOrAccumulate(request);
    return ResponseEntity.ok(BaseResponse.success("스탬프 적립이 완료되었습니다.", response));
  }

  @Operation(
      summary = "완성 임박 스탬프카드 단건 조회",
      description =
          """
        현재 로그인 사용자의 스탬프카드 중 **완성에 가장 임박한 카드 1장**을 반환합니다. (로그인 필요)

        - 대상: 상태가 **IN_PROGRESS** 인 카드
        - 정렬 우선순위:
          1) **count % 10** **내림차순** (완성까지 남은 칸이 가장 적은 카드 우선)
          2) **updatedAt **내림차순** (동일 개수일 때 더 최근에 적립/수정된 카드 우선)
        - 없으면 **data: null** 로 반환
        """)
  @GetMapping("/expiring")
  public ResponseEntity<BaseResponse<StampResponse>> getMostExpiringStamp() {
    StampResponse response = stampService.getMostExpiringStamp();
    String message = (response == null) ? "아직 스탬프카드가 없어요! 스탬프카드를 생성해주세요." : "완성 임박 스탬프카드 조회 성공";
    return ResponseEntity.ok(BaseResponse.success(message, response));
  }
}
