package com.likelion.danchu.domain.mission.service;

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
import com.likelion.danchu.global.exception.CustomException;
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
}
