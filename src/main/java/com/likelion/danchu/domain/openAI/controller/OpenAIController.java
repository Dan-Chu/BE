package com.likelion.danchu.domain.openAI.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.danchu.domain.openAI.dto.response.MissionRecommendResponse;
import com.likelion.danchu.domain.openAI.dto.response.StoreRecommendResponse;
import com.likelion.danchu.domain.openAI.service.MissionRecommendService;
import com.likelion.danchu.domain.openAI.service.StoreRecommendService;
import com.likelion.danchu.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/openai")
@Tag(name = "OpenAI", description = "OpenAI 관련 API")
public class OpenAIController {

  private final StoreRecommendService storeRecommendService;
  private final MissionRecommendService missionRecommendService;

  @Operation(
      summary = "맞춤형 가게 추천",
      description =
          """
              로그인한 사용자의 취향/이력 기반으로 **최대 5개** 가게를 추천합니다.

              처리 절차
              1) 내부 정렬(결정 규칙)
                 - k1 = 사용자 해시태그 ∩ 가게 해시태그 개수 **내림차순**
                 - k1 동률 시 k2 = 사용자가 참여한 미션 중 해당 가게 횟수 **내림차순**
                 - 그래도 동률이면 storeId **내림차순**
              2) 후보 5개에 한해 **임베딩 재랭킹** 수행
                 - 입력 A: 사용자 해시태그 텍스트(“#” 제거 후 공백 결합)
                 - 입력 B: 각 가게의 description + 해시태그 텍스트
                 - `text-embedding-3-small` 임베딩 코사인 유사도 기준 **내림차순**
                 - 임베딩 호출 실패 시 1)에서 얻은 순서를 그대로 사용
              3) 응답에 가게 해시태그 포함

              요청 요건
              - **로그인 필수(JWT)**

              응답
              - 200 OK: BaseResponse<List<StoreRecommendResponse>>
              """)
  @GetMapping("/stores/recommend")
  public ResponseEntity<BaseResponse<List<StoreRecommendResponse>>> recommendStores() {

    List<StoreRecommendResponse> result = storeRecommendService.recommendTop5StoresForCurrentUser();
    return ResponseEntity.ok(BaseResponse.success("추천 가게 5개 조회 성공", result));
  }

  @Operation(
      summary = "맞춤형 미션 추천",
      description =
          """
              로그인한 사용자의 관심 해시태그/이력 기반으로 1개의 미션을 추천합니다.

              처리 절차
              1. 기본 점수 계산
                - 사용자 관심 해시태그와 가게 해시태그의 교집합 개수
                - 해당 미션의 완료 횟수(인기 정도)
                - missionId 내림차순 정렬
                - 상위 5개 후보 선별
              2. 임베딩 기반 재랭킹
                - A (사용자): 사용자 해시태그를 # 제거 후 공백으로 합친 문자열
                - B (미션): 미션 제목 + 미션 설명 + 해당 가게 해시태그
              3. 최종 추천
                - 재랭킹된 리스트에서 첫 번째 미션만 반환
              """)
  @GetMapping("/missions/recommend")
  public ResponseEntity<BaseResponse<MissionRecommendResponse>> recommendMission() {
    MissionRecommendResponse result = missionRecommendService.recommendTopMissionForCurrentUser();
    return ResponseEntity.ok(BaseResponse.success("추천 미션 조회 성공", result));
  }
}
