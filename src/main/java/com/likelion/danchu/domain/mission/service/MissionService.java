package com.likelion.danchu.domain.mission.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.likelion.danchu.domain.mission.dto.request.MissionRequest;
import com.likelion.danchu.domain.mission.dto.response.MissionResponse;
import com.likelion.danchu.domain.mission.entity.Mission;
import com.likelion.danchu.domain.mission.exception.MissionErrorCode;
import com.likelion.danchu.domain.mission.mapper.MissionMapper;
import com.likelion.danchu.domain.mission.repository.MissionRepository;
import com.likelion.danchu.domain.store.entity.Store;
import com.likelion.danchu.domain.store.repository.StoreRepository;
import com.likelion.danchu.domain.user.entity.User;
import com.likelion.danchu.domain.user.exception.UserErrorCode;
import com.likelion.danchu.domain.user.repository.UserRepository;
import com.likelion.danchu.global.exception.CustomException;
import com.likelion.danchu.global.redis.RedisUtil;
import com.likelion.danchu.global.s3.entity.PathName;
import com.likelion.danchu.global.s3.service.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionService {

  private final StoreRepository storeRepository;
  private final S3Service s3Service;
  private final MissionRepository missionRepository;
  private final MissionMapper missionMapper;
  private final UserRepository userRepository;
  private final RedisUtil redisUtil;

  /**
   * 미션 생성 - 가게 존재 여부 검증
   *
   * @param missionRequest 미션 생성 요청 본문(JSON)
   * @param imageFile 보상 이미지 파일(선택)
   * @return 생성된 미션 응답
   */
  public MissionResponse createMission(MissionRequest missionRequest, MultipartFile imageFile) {

    // 가게 존재 여부 확인
    Store store =
        storeRepository
            .findById(missionRequest.getStoreId())
            .orElseThrow(() -> new CustomException(MissionErrorCode.STORE_NOT_FOUND));

    // 중복 미션 존재 여부 확인
    boolean exists =
        missionRepository.existsByStoreIdAndDateAndTitle(
            missionRequest.getStoreId(), missionRequest.getDate(), missionRequest.getTitle());
    if (exists) {
      throw new CustomException(MissionErrorCode.DUPLICATE_MISSION);
    }

    // (선택) 이미지 업로드
    String rewardImageUrl = null;
    if (imageFile != null && !imageFile.isEmpty()) {
      try {
        rewardImageUrl = s3Service.uploadImage(PathName.REWARD, imageFile).getImageUrl();
      } catch (Exception e) {
        throw new CustomException(MissionErrorCode.REWARD_UPLOAD_FAILED);
      }
    }
    Mission saved =
        missionRepository.save(missionMapper.toEntity(store, missionRequest, rewardImageUrl));
    return missionMapper.toResponse(saved);
  }

  /**
   * 오늘 날짜의 미션 중에서 로그인 사용자가 아직 완료하지 않은 미션만 조회
   *
   * @param userId 로그인한 사용자 ID
   * @return 오늘의 미완료 미션 목록을 {@link MissionResponse} 리스트로 반환
   */
  public List<MissionResponse> getTodayMissions(Long userId) {
    // Asia/Seoul 타임존 기준으로 오늘 날짜 계산
    LocalDate todayKST = LocalDate.now(ZoneId.of("Asia/Seoul"));

    // 네이티브 쿼리: user_completed_mission 조인테이블을 통해 미완료만 필터링
    return missionRepository.findTodayNotCompleted(todayKST, userId).stream()
        .map(missionMapper::toResponse)
        .toList();
  }

  /**
   * 미션 완료 처리 유저의 완료 미션 ID 리스트에 missionId를 추가한다(중복 시 무시) 새로 추가된 경우에만 완료 카운트/Redis 값을 증가시킨다.
   *
   * @param userId 완료 처리하는 사용자 ID
   * @param missionId 완료할 미션 ID
   * @throws CustomException 미션 또는 사용자 미존재 시 예외 발생
   */
  public void completeMission(Long userId, Long missionId) {
    // (옵션) 미션 존재 검증
    missionRepository
        .findById(missionId)
        .orElseThrow(() -> new CustomException(MissionErrorCode.MISSION_NOT_FOUND));

    // 사용자 조회
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 완료 미션 ID 추가 (이미 있으면 false)
    boolean added = user.addCompletedMissionId(missionId);
    if (!added) {
      // 이미 완료한 미션이면 아무 작업도 하지 않음 (멱등성 보장)
      return;
    }

    // 카운트/Redis 증가 로직
    increaseCompletedMission(userId);
  }

  /**
   * 사용자 완료 미션 카운트를 증가시키고 Redis 값도 동기화
   *
   * @param userId 완료 카운트를 증가시킬 사용자 ID
   * @throws CustomException 사용자 미존재 시 예외 발생
   */
  public void increaseCompletedMission(Long userId) {
    String redisKey = "user:completedMission:" + userId;

    // 1. DB에서 유저 조회
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 2. 엔티티 값 증가
    user.increaseCompletedMission();

    // 3. Redis 값 증가/초기화
    if (redisUtil.existData(redisKey)) {
      // Redis 값이 있으면 바로 +1
      long newValue = redisUtil.getLongValue(redisKey) + 1;
      redisUtil.setData(redisKey, String.valueOf(newValue));
    } else {
      // Redis에 없으면 DB값을 그대로 넣음
      redisUtil.setData(redisKey, String.valueOf(user.getCompletedMission()));
    }
  }

  /**
   * 미션 상세 조회
   *
   * @param missionId 조회할 미션의 ID
   * @return 해당 ID의 미션 상세 응답
   * @throws CustomException 미션이 존재하지 않는 경우
   */
  public MissionResponse getMission(Long missionId) {
    Mission mission =
        missionRepository
            .findById(missionId)
            .orElseThrow(() -> new CustomException(MissionErrorCode.MISSION_NOT_FOUND));

    return missionMapper.toResponse(mission);
  }
}
