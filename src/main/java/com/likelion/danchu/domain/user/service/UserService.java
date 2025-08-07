package com.likelion.danchu.domain.user.service;

import jakarta.transaction.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.likelion.danchu.domain.user.dto.request.UserRequest.InfoRequest;
import com.likelion.danchu.domain.user.dto.response.UserResponse;
import com.likelion.danchu.domain.user.entity.User;
import com.likelion.danchu.domain.user.exception.UserErrorCode;
import com.likelion.danchu.domain.user.mapper.UserMapper;
import com.likelion.danchu.domain.user.repository.UserRepository;
import com.likelion.danchu.global.exception.CustomException;
import com.likelion.danchu.global.redis.RedisUtil;
import com.likelion.danchu.global.s3.entity.PathName;
import com.likelion.danchu.global.s3.service.S3Service;
import com.likelion.danchu.global.security.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  private final SecurityUtil securityUtil;
  private final PasswordEncoder passwordEncoder;

  private final S3Service s3Service;
  private final UserMapper userMapper;
  private final UserRepository userRepository;
  private final RedisUtil redisUtil;

  /**
   * 회원가입을 처리하는 메서드
   *
   * @param userRequest 회원 가입에 필요한 사용자 정보 요청 객체 (닉네임, 이메일, 비밀번호 포함)
   * @param imageFile 프로필 이미지 파일 (nullable, 비어있을 수 있음)
   * @return 등록된 사용자 정보가 담긴 {@link UserResponse} 객체
   * @throws CustomException 닉네임 또는 이메일 중복 시 {@link UserErrorCode#DUPLICATE_NICKNAME}, {@link
   *     UserErrorCode#DUPLICATE_EMAIL}
   * @throws CustomException 이미지 업로드 실패 시 {@link UserErrorCode#IMAGE_UPLOAD_FAILED}
   * @throws CustomException 사용자 저장 실패 시 {@link UserErrorCode#USER_SAVE_FAILED}
   */
  public UserResponse register(InfoRequest userRequest, MultipartFile imageFile) {
    // nickname 중복 체크
    String nickname = userRequest.getNickname();
    if (userRepository.existsByNickname(nickname)) {
      throw new CustomException(UserErrorCode.DUPLICATE_NICKNAME);
    }

    // email 중복 체크
    String email = userRequest.getEmail();
    if (userRepository.existsByEmail(email)) {
      throw new CustomException(UserErrorCode.DUPLICATE_EMAIL);
    }

    // 이미지 업로드 (PathName USER 폴더에 저장)
    String imageUrl = null;
    if (imageFile != null && !imageFile.isEmpty()) {
      try {
        imageUrl = s3Service.uploadImage(PathName.USER, imageFile).getImageUrl();
      } catch (Exception e) {
        throw new CustomException(UserErrorCode.IMAGE_UPLOAD_FAILED);
      }
    }

    String encodedPassword = passwordEncoder.encode(userRequest.getPassword());

    // User Entity 생성하여 DB에 저장
    User user;
    try {
      user = userRepository.save(userMapper.toEntity(nickname, email, encodedPassword, imageUrl));
    } catch (Exception e) {
      throw new CustomException(UserErrorCode.USER_SAVE_FAILED);
    }

    String redisKey = "user:completedMission:" + user.getId();
    redisUtil.setData(redisKey, "0");

    return userMapper.toResponse(user, getCompletedMission(user.getId()));
  }

  /**
   * 현재 로그인한 사용자의 완료한 미션 개수를 조회하는 메서드
   *
   * <p>내부적으로 Redis 캐시에 값이 존재하는 경우 이를 사용하고, 존재하지 않으면 DB에서 조회한 뒤 Redis에 저장합니다.
   *
   * @return 완료한 미션 개수
   * @throws CustomException 사용자 정보를 찾을 수 없는 경우 {@link UserErrorCode#USER_NOT_FOUND}
   */
  public long getCompletedMissionCountForCurrentUser() {

    Long userId = securityUtil.getCurrentUserId();
    return getCompletedMission(userId);
  }

  // Lazy Loading: Redis에 없으면 → DB에서 조회하고 → Redis에 저장
  public long getCompletedMission(Long userId) {
    String redisKey = "user:completedMission:" + userId;

    if (redisUtil.existData(redisKey)) {
      return redisUtil.getLongValue(redisKey);
    }

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    long count = user.getCompletedMission();

    redisUtil.setData(redisKey, String.valueOf(count));

    return count;
  }
}
