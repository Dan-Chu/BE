package com.likelion.danchu.domain.coupon.service;

import java.time.LocalDate;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.likelion.danchu.domain.coupon.dto.request.CouponRequest;
import com.likelion.danchu.domain.coupon.dto.response.CouponResponse;
import com.likelion.danchu.domain.coupon.entity.Coupon;
import com.likelion.danchu.domain.coupon.exception.CouponErrorCode;
import com.likelion.danchu.domain.coupon.mapper.CouponMapper;
import com.likelion.danchu.domain.coupon.repository.CouponRepository;
import com.likelion.danchu.domain.mission.entity.Mission;
import com.likelion.danchu.domain.mission.exception.MissionErrorCode;
import com.likelion.danchu.domain.mission.repository.MissionRepository;
import com.likelion.danchu.domain.store.entity.Store;
import com.likelion.danchu.domain.store.exception.StoreErrorCode;
import com.likelion.danchu.domain.store.repository.StoreRepository;
import com.likelion.danchu.domain.user.entity.User;
import com.likelion.danchu.domain.user.exception.UserErrorCode;
import com.likelion.danchu.domain.user.repository.UserRepository;
import com.likelion.danchu.global.exception.CustomException;
import com.likelion.danchu.global.s3.entity.PathName;
import com.likelion.danchu.global.s3.service.S3Service;
import com.likelion.danchu.global.security.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {

  private final CouponRepository couponRepository;
  private final CouponMapper couponMapper;
  private final UserRepository userRepository;
  private final StoreRepository storeRepository;
  private final MissionRepository missionRepository;
  private final S3Service s3Service;

  /**
   * 쿠폰 생성을 처리하는 메서드
   *
   * @param request 쿠폰 생성에 필요한 요청 객체 (가게 ID, 보상 내용 포함)
   * @param imageFile 쿠폰 이미지 파일 (null 불가, 비어있으면 예외 발생)
   * @return 생성된 쿠폰 정보를 담은 {@link CouponResponse} 객체
   * @throws CustomException 로그인한 사용자를 찾을 수 없는 경우 {@link UserErrorCode#USER_NOT_FOUND}
   * @throws CustomException 가게를 찾을 수 없는 경우 {@link StoreErrorCode#STORE_NOT_FOUND}
   * @throws CustomException 보상 내용이 비어있거나 유효하지 않은 경우 {@link CouponErrorCode#INVALID_REWARD}
   * @throws CustomException 이미지가 없거나 비어있는 경우 {@link CouponErrorCode#IMAGE_REQUIRED}
   * @throws CustomException 이미지 업로드 실패 시 {@link CouponErrorCode#IMAGE_UPLOAD_FAILED}
   * @throws CustomException 쿠폰 저장 실패 시 {@link CouponErrorCode#COUPON_SAVE_FAILED}
   */
  public CouponResponse createCoupon(CouponRequest.CreateRequest request, MultipartFile imageFile) {
    // 현재 로그인 유저
    Long userId = SecurityUtil.getCurrentUserId();
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 가게 조회
    Store store =
        storeRepository
            .findById(request.getStoreId())
            .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

    String reward = normalizeReward(request.getReward());
    String imageUrl = uploadRequiredImage(imageFile);

    // 엔티티 생성 -> 저장
    Coupon coupon = couponMapper.toEntity(user, store, reward, imageUrl);
    Coupon saved = couponRepository.save(coupon);

    // 응답 변환
    return couponMapper.toResponse(saved);
  }

  /**
   * 만료되지 않은 쿠폰 목록을 만료 임박순으로 전체 조회합니다.
   *
   * <p>오늘(LocalDate.now()) 기준으로 만료일(expirationDate)이 오늘 이후인 쿠폰만 반환하며, 만료일이 가까운 순서(오름차순)로 정렬됩니다.
   *
   * @return List<CouponResponse> 만료 임박순으로 정렬된 쿠폰 응답 DTO 리스트 (만료 쿠폰이 없으면 빈 리스트 반환)
   * @throws CustomException 쿠폰 조회 중 DB 접근 오류나 예기치 못한 오류가 발생한 경우
   */
  public List<CouponResponse> getAllValidCoupons() {
    try {
      LocalDate today = LocalDate.now();

      // DB에서 만료 제외 + 임박순으로 로드
      List<Coupon> coupons =
          couponRepository.findAllByExpirationDateGreaterThanEqualOrderByExpirationDateAsc(today);

      // 빈 리스트는 정상 (예외 아님)
      if (coupons.isEmpty()) {
        return List.of();
      }

      return couponMapper.toResponseList(coupons);

    } catch (Exception e) {
      throw new CustomException(CouponErrorCode.COUPON_FETCH_FAILED);
    }
  }

  /**
   * 쿠폰 사용(삭제) 처리 로직입니다.
   *
   * <p>요청 바디의 인증코드(authCode)로 가게를 검증한 뒤, PathVariable의 {@code couponId}에 해당하는 쿠폰을 소유자/가게 일치 여부 확인 후
   * **삭제 처리**합니다.
   *
   * @param couponId 삭제(사용)할 쿠폰 ID
   * @param request 가게 인증코드가 담긴 요청 바디
   * @throws com.likelion.danchu.global.exception.CustomException 사용자 없음({@code USER_NOT_FOUND}),
   *     인증코드 무효({@code INVALID_AUTH_CODE}), 쿠폰 미존재({@code COUPON_NOT_FOUND}), 소유자 불일치({@code
   *     COUPON_OWNER_MISMATCH}), 가게 불일치({@code COUPON_STORE_MISMATCH}), 삭제 실패({@code
   *     COUPON_DELETE_FAILED}) 시 발생
   */
  public void useCoupon(Long couponId, CouponRequest.UseRequest request) {
    // 1) 현재 로그인 사용자
    Long userId = SecurityUtil.getCurrentUserId();
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 2) 인증코드로 가게 조회
    Store store =
        storeRepository
            .findByAuthCode(request.getAuthCode())
            .orElseThrow(() -> new CustomException(CouponErrorCode.INVALID_AUTH_CODE));

    // 3) 쿠폰 조회
    Coupon coupon =
        couponRepository
            .findById(couponId)
            .orElseThrow(() -> new CustomException(CouponErrorCode.COUPON_NOT_FOUND));

    // 4) 가게 검증
    if (!coupon.getUser().getId().equals(user.getId())) {
      throw new CustomException(CouponErrorCode.COUPON_OWNER_MISMATCH);
    }
    if (!coupon.getStore().getId().equals(store.getId())) {
      throw new CustomException(CouponErrorCode.COUPON_STORE_MISMATCH);
    }

    // 5) 삭제
    try {
      couponRepository.delete(coupon);
    } catch (DataAccessException e) {
      throw new CustomException(CouponErrorCode.COUPON_DELETE_FAILED);
    }
  }

  public CouponResponse createCouponFromMission(Long missionId, Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    Mission mission =
        missionRepository
            .findById(missionId)
            .orElseThrow(() -> new CustomException(MissionErrorCode.MISSION_NOT_FOUND));

    Store store = mission.getStore();
    if (store == null) {
      throw new CustomException(StoreErrorCode.STORE_NOT_FOUND);
    }
    String reward = normalizeReward(mission.getReward());

    // 미션에서 설정된 이미지가 없다면 가게 메인 이미지 사용하여 쿠폰 생성
    String imageUrl = mission.getRewardImageUrl();
    if (imageUrl == null || imageUrl.isBlank()) {
      imageUrl = store.getMainImageUrl();
    }

    Coupon saved = saveCoupon(user, store, reward, imageUrl);
    return couponMapper.toResponse(saved);
  }

  private String normalizeReward(String reward) {
    if (reward == null || reward.trim().isEmpty()) {
      throw new CustomException(CouponErrorCode.INVALID_REWARD);
    }
    return reward.trim();
  }

  private String uploadRequiredImage(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new CustomException(CouponErrorCode.IMAGE_REQUIRED);
    }
    try {
      // 쿠폰이면 COUPON 버킷/폴더 사용 추천
      return s3Service.uploadImage(PathName.REWARD, file).getImageUrl();
    } catch (Exception e) {
      throw new CustomException(CouponErrorCode.IMAGE_UPLOAD_FAILED);
    }
  }

  private Coupon saveCoupon(User user, Store store, String reward, String imageUrl) {
    try {
      Coupon coupon = couponMapper.toEntity(user, store, reward, imageUrl);
      return couponRepository.save(coupon);
    } catch (Exception e) {
      throw new CustomException(CouponErrorCode.COUPON_SAVE_FAILED);
    }
  }

  public CouponResponse createCouponFromStore(Long storeId, Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    Store store =
        storeRepository
            .findById(storeId)
            .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

    String reward = normalizeReward(store.getStampReward());
    String imageUrl = store.getMainImageUrl();

    try {
      Coupon toSave = couponMapper.toEntity(user, store, reward, imageUrl);
      Coupon saved = couponRepository.save(toSave);
      return couponMapper.toResponse(saved);
    } catch (DataAccessException e) {
      throw new CustomException(CouponErrorCode.COUPON_SAVE_FAILED);
    }
  }
}
