package com.likelion.danchu.domain.stamp.service;

import java.util.Comparator;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.likelion.danchu.domain.stamp.dto.request.StampRequest;
import com.likelion.danchu.domain.stamp.dto.response.StampResponse;
import com.likelion.danchu.domain.stamp.entity.Stamp;
import com.likelion.danchu.domain.stamp.entity.StampStatus;
import com.likelion.danchu.domain.stamp.exception.StampErrorCode;
import com.likelion.danchu.domain.stamp.mapper.StampMapper;
import com.likelion.danchu.domain.stamp.repository.StampRepository;
import com.likelion.danchu.domain.store.entity.Store;
import com.likelion.danchu.domain.store.repository.StoreRepository;
import com.likelion.danchu.domain.user.entity.User;
import com.likelion.danchu.domain.user.exception.UserErrorCode;
import com.likelion.danchu.domain.user.repository.UserRepository;
import com.likelion.danchu.global.exception.CustomException;
import com.likelion.danchu.global.security.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StampService {

  private final UserRepository userRepository;
  private final StampRepository stampRepository;
  private final StampMapper stampMapper;
  private final StoreRepository storeRepository;

  /**
   * 인증코드로 가게를 식별해 스탬프를 생성/적립합니다.
   *
   * <p>비즈니스 규칙:
   *
   * <ul>
   *   <li>인증코드에 해당하는 가게가 없으면 {@link StampErrorCode#INVALID_AUTH_CODE} 예외 발생
   *   <li>해당 유저+가게 조합의 스탬프카드가 없으면 새 카드 생성 (count=1, status=IN_PROGRESS)
   *   <li>카드가 이미 READY_TO_CLAIM 상태면 {@link StampErrorCode#ALREADY_READY_TO_CLAIM} 예외 발생
   *   <li>카드가 IN_PROGRESS 상태면 count 증가 후, 총 개수가 10의 배수이면 READY_TO_CLAIM으로 변경
   * </ul>
   *
   * @param request 인증코드(`authCode`)를 포함한 요청 DTO
   * @return 적립 또는 생성된 스탬프카드의 응답 DTO
   * @throws CustomException 잘못된 인증코드, 사용자/가게 미존재, 이미 수령 대기 상태, 잘못된 상태, 저장 실패 등
   */
  public StampResponse createOrAccumulate(StampRequest request) {
    try {
      // 1) 현재 로그인 사용자 조회
      Long userId = SecurityUtil.getCurrentUserId();
      User user =
          userRepository
              .findById(userId)
              .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

      // 2) 인증코드로 가게 조회
      Store store =
          storeRepository
              .findByAuthCode(request.getAuthCode())
              .orElseThrow(() -> new CustomException(StampErrorCode.INVALID_AUTH_CODE));

      // 3) 동일 스탬프카드(해당 유저+가게의 최신 카드) 조회
      Stamp latest =
          stampRepository
              .findTopByUser_IdAndStore_IdOrderByIdDesc(user.getId(), store.getId())
              .orElse(null);

      if (latest == null) {
        // 3-1) 없으면 새로 스탬프카드 생성 (보상은 가게 정책에 맞게 매핑)
        Stamp created = stampMapper.toEntity(user, store);
        Stamp saved = stampRepository.save(created);
        return stampMapper.toResponse(saved);
      }

      // 3-2) 있으면 상태 분기
      if (latest.getStatus() == StampStatus.READY_TO_CLAIM) {
        // 이미 수령 대기 상태
        throw new CustomException(StampErrorCode.ALREADY_READY_TO_CLAIM);
      }

      if (latest.getStatus() == StampStatus.IN_PROGRESS) {
        latest.incrementCount();
        if (latest.getCurrentCount() == 0) { // 10의 배수라면 수령 대기로 변경
          latest.updateStatus(StampStatus.READY_TO_CLAIM);
        }
        latest = stampRepository.save(latest);

        return stampMapper.toResponse(latest);
      }
      throw new CustomException(StampErrorCode.INVALID_STAMP_STATUS);

    } catch (CustomException ce) {
      throw ce;
    } catch (DataAccessException dae) {
      throw new CustomException(StampErrorCode.STAMP_SAVE_FAILED);
    } catch (Exception e) {
      throw new CustomException(StampErrorCode.STAMP_SAVE_FAILED);
    }
  }

  /**
   * 현재 로그인 사용자의 스탬프카드 중 "완성 임박(= count % 10이 큰)" 카드 1장을 반환합니다.
   *
   * <p>대상은 {@code IN_PROGRESS} 상태의 카드만이며, 정렬 우선순위는 (1) {@code currentCount = count % 10} 내림차순, (2)
   * {@code updatedAt} 내림차순입니다.
   *
   * @return StampResponse 가장 임박한 카드 1장 (없으면 null)
   * @throws CustomException 사용자 미존재 또는 DB 조회 오류 시
   */
  @Transactional
  public StampResponse getMostExpiringStamp() {
    // 1) 현재 로그인 사용자
    Long userId = SecurityUtil.getCurrentUserId();
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    try {
      // 2) 유저의 모든 스탬프 카드 조회 (최근 수정 순으로 가져오면 tie-break에 유리)
      List<Stamp> stamps = stampRepository.findAllByUser_IdOrderByUpdatedAtDesc(user.getId());

      // 3) IN_PROGRESS만 대상으로, currentCount DESC → updatedAt DESC 로 1개 선택
      Stamp candidate =
          stamps.stream()
              .filter(s -> s.getStatus() == StampStatus.IN_PROGRESS)
              .max(
                  Comparator.comparingInt(Stamp::getCurrentCount) // 1순위: count%10
                      .thenComparing(Stamp::getUpdatedAt)) // 2순위: updatedAt
              .orElse(null);

      return candidate == null ? null : stampMapper.toResponse(candidate);

    } catch (CustomException ce) {
      throw ce;
    } catch (RuntimeException e) {
      throw new CustomException(StampErrorCode.STAMP_FETCH_FAILED);
    }
  }

  /**
   * 현재 로그인 사용자의 모든 스탬프카드를 최근 수정 순으로 전체 조회합니다.
   *
   * <p>정렬 기준: {@code updatedAt DESC} (가장 최근에 적립/변경된 카드가 먼저).
   *
   * @return List<StampResponse> 사용자가 가진 스탬프카드 목록 (없으면 빈 리스트)
   * @throws CustomException 사용자 미존재 또는 DB 조회 중 오류가 발생한 경우
   */
  public List<StampResponse> getMyStamps() {
    Long userId = SecurityUtil.getCurrentUserId();
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    try {
      List<Stamp> stamps = stampRepository.findAllByUser_IdOrderByUpdatedAtDesc(user.getId());
      return stampMapper.toResponseList(stamps);
    } catch (RuntimeException e) {
      throw new CustomException(StampErrorCode.STAMP_FETCH_FAILED);
    }
  }
}
