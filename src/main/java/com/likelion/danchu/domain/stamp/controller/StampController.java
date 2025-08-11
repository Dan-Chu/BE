package com.likelion.danchu.domain.stamp.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.danchu.domain.coupon.dto.response.CouponResponse;
import com.likelion.danchu.domain.stamp.dto.request.StampRequest;
import com.likelion.danchu.domain.stamp.dto.response.StampResponse;
import com.likelion.danchu.domain.stamp.service.StampService;
import com.likelion.danchu.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
      summary = "보상 수령 (쿠폰 발급)",
      description =
          """
        스탬프 10개 적립 후 활성화되는 '보상 수령하기' 동작입니다. (로그인 필요)

        - 검증:
          1) 해당 스탬프카드가 존재/본인 소유인지 확인
          2) 상태가 **READY_TO_CLAIM** 이며 **count % 10 == 0** 인지 확인
        - 처리:
          1) 가게의 **stampReward**/**mainImageUrl** 기준으로 **쿠폰 발급**
          2) 스탬프카드 **count++**, **IN_PROGRESS** 로 되돌림(다음 스탬프카드 시작)
        """)
  @PostMapping("/{stampId}/use")
  public ResponseEntity<BaseResponse<CouponResponse>> claimReward(
      @Parameter(description = "보상 수령할 스탬프카드 ID", example = "1") @PathVariable Long stampId) {

    CouponResponse coupon = stampService.claimReward(stampId);
    return ResponseEntity.ok(BaseResponse.success("쿠폰 수령이 완료되었습니다.", coupon));
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

  @Operation(
      summary = "내 스탬프카드 전체 조회(최근 수정 순)",
      description =
          """
      현재 로그인한 사용자가 보유한 모든 스탬프카드를 **최근 수정 순(내림차순)** 으로 반환합니다. (로그인 필요)

      - 정렬: updatedAt DESC (마지막 적립/상태변경 시점이 최신일수록 먼저)
      - 반환: id, storeName, reward, currentCount, cardNum, status
      - 비고: 없으면 빈 배열([]) 반환
      """)
  @GetMapping("")
  public ResponseEntity<BaseResponse<List<StampResponse>>> getMyStamps() {
    List<StampResponse> response = stampService.getMyStamps();
    return ResponseEntity.ok(BaseResponse.success("스탬프카드 목록 조회 성공", response));
  }
}
