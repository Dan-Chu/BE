package com.likelion.danchu.domain.coupon.service;

import jakarta.transaction.Transactional;

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
    String imageUrl = mission.getRewardImageUrl();

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
}
