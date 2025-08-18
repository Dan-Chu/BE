package com.likelion.danchu.domain.user.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.likelion.danchu.domain.coupon.entity.Coupon;
import com.likelion.danchu.domain.coupon.repository.CouponRepository;
import com.likelion.danchu.domain.hashtag.dto.request.HashtagRequest;
import com.likelion.danchu.domain.hashtag.entity.Hashtag;
import com.likelion.danchu.domain.hashtag.exception.HashtagErrorCode;
import com.likelion.danchu.domain.hashtag.repository.HashtagRepository;
import com.likelion.danchu.domain.stamp.repository.StampRepository;
import com.likelion.danchu.domain.user.dto.request.UserRequest;
import com.likelion.danchu.domain.user.dto.request.UserRequest.InfoRequest;
import com.likelion.danchu.domain.user.dto.response.UserResponse;
import com.likelion.danchu.domain.user.entity.User;
import com.likelion.danchu.domain.user.entity.UserHashtag;
import com.likelion.danchu.domain.user.exception.UserErrorCode;
import com.likelion.danchu.domain.user.mapper.UserMapper;
import com.likelion.danchu.domain.user.repository.UserHashtagRepository;
import com.likelion.danchu.domain.user.repository.UserRepository;
import com.likelion.danchu.global.exception.CustomException;
import com.likelion.danchu.global.security.SecurityUtil;
import com.likelion.danchu.infra.s3.entity.PathName;
import com.likelion.danchu.infra.s3.service.S3Service;

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
  private final HashtagRepository hashtagRepository;
  private final UserHashtagRepository userHashtagRepository;
  private final CouponRepository couponRepository;
  private final StampRepository stampRepository;

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

    return userMapper.toResponse(user, 0, null);
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

  /**
   * 회원 정보를 수정하는 메서드
   *
   * @param request 닉네임, 이메일, 관심 해시태그가 포함된 사용자 수정 요청 DTO
   * @param imageFile 새로운 프로필 이미지 파일 (nullable, 비어있을 수 있음)
   * @return 수정된 사용자 정보를 담은 {@link UserResponse} 객체
   * @throws CustomException 사용자 조회 실패 시 {@link UserErrorCode#USER_NOT_FOUND}
   * @throws CustomException 닉네임 또는 이메일 중복 시 {@link UserErrorCode#DUPLICATE_NICKNAME}, {@link
   *     UserErrorCode#DUPLICATE_EMAIL}
   * @throws CustomException 이미지 업로드 실패 시 {@link UserErrorCode#IMAGE_UPLOAD_FAILED}
   * @throws CustomException 사용자 저장 실패 시 {@link UserErrorCode#USER_SAVE_FAILED}
   */
  public UserResponse updateUser(UserRequest.UpdateRequest request, MultipartFile imageFile) {
    Long userId = securityUtil.getCurrentUserId();

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    String nickname = request.getNickname();
    if (!user.getNickname().equals(nickname) && userRepository.existsByNickname(nickname)) {
      throw new CustomException(UserErrorCode.DUPLICATE_NICKNAME);
    }

    String email = request.getEmail();
    if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
      throw new CustomException(UserErrorCode.DUPLICATE_EMAIL);
    }

    // 프로필 이미지 업로드
    String imageUrl = user.getProfileImageUrl();
    if (imageFile != null && !imageFile.isEmpty()) {
      try {
        imageUrl = s3Service.uploadImage(PathName.USER, imageFile).getImageUrl();
      } catch (Exception e) {
        throw new CustomException(UserErrorCode.IMAGE_UPLOAD_FAILED);
      }
    }

    // 사용자 정보 수정
    user.updateInfo(nickname, email, imageUrl);

    // 관심 해시태그 수정
    updateUserHashtags(user, request.getHashtags());

    List<Hashtag> hashtags = getUserHashtags(user);
    return userMapper.toResponse(user, getCompletedMission(userId), hashtags);
  }

  /**
   * 현재 로그인한 사용자의 정보를 조회하는 메서드
   *
   * @return 로그인한 사용자의 정보를 담은 {@link UserResponse} 객체
   * @throws CustomException 사용자 조회 실패 시 {@link UserErrorCode#USER_NOT_FOUND}
   */
  public UserResponse getUserInfo() {
    Long userId = SecurityUtil.getCurrentUserId();

    // 사용자 조회
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 관심 해시태그 조회
    List<Hashtag> hashtags = getUserHashtags(user);

    // 완료 미션 개수 조회
    long completedMission = getCompletedMission(userId);

    // DTO 변환
    return userMapper.toResponse(user, completedMission, hashtags);
  }

  private void updateUserHashtags(User user, List<HashtagRequest> newRequests) {
    if (user == null) {
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }

    // 기존 유저 해시태그 조회
    List<UserHashtag> currentUserHashtags;
    try {
      currentUserHashtags = userHashtagRepository.findAllByUser(user);
    } catch (Exception e) {
      throw new CustomException(UserErrorCode.USER_SAVE_FAILED);
    }

    Set<String> currentHashtagNames =
        currentUserHashtags.stream()
            .map(uh -> uh.getHashtag().getName())
            .collect(Collectors.toSet());

    // 새로운 해시태그 정리 (중복 제거 + 포맷팅)
    Set<String> newFormattedNames =
        newRequests == null
            ? Set.of()
            : newRequests.stream().map(HashtagRequest::toFormattedName).collect(Collectors.toSet());

    // 삭제할 항목 = 현재 DB에 있는데 요청에는 없는 것
    Set<String> hashtagsToDelete = new HashSet<>(currentHashtagNames);
    hashtagsToDelete.removeAll(newFormattedNames);

    // 추가할 항목 = 요청에는 있는데 현재 DB에 없는 것
    Set<String> hashtagsToAdd = new HashSet<>(newFormattedNames);
    hashtagsToAdd.removeAll(currentHashtagNames);

    // 삭제 처리
    List<UserHashtag> toDelete =
        currentUserHashtags.stream()
            .filter(uh -> hashtagsToDelete.contains(uh.getHashtag().getName()))
            .toList();

    try {
      userHashtagRepository.deleteAll(toDelete);
    } catch (Exception e) {
      throw new CustomException(UserErrorCode.USER_SAVE_FAILED);
    }

    for (String name : hashtagsToAdd) {
      if (name.length() < 2 || name.length() > 11) {
        throw new CustomException(HashtagErrorCode.HASHTAG_LENGTH_INVALID);
      }

      Hashtag hashtag =
          hashtagRepository
              .findByName(name)
              .orElseThrow(() -> new CustomException(HashtagErrorCode.HASHTAG_NOT_FOUND));

      try {
        userHashtagRepository.save(UserHashtag.builder().user(user).hashtag(hashtag).build());
      } catch (Exception e) {
        throw new CustomException(UserErrorCode.USER_SAVE_FAILED);
      }
    }
  }

  public List<Hashtag> getUserHashtags(User user) {
    return userHashtagRepository.findAllByUser(user).stream().map(UserHashtag::getHashtag).toList();
  }

  public long getCompletedMission(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new CustomException(UserErrorCode.USER_NOT_FOUND);
    }
    return userRepository.getCompletedMissionCount(userId);
  }

  /**
   * 회원 탈퇴(하드 삭제): S3/DB/Redis의 사용자 관련 자원을 모두 정리합니다.
   *
   * <p>순서: 1) S3 이미지 삭제 (사용자 프로필 + 사용자의 모든 쿠폰 이미지) 2) DB 자원 삭제 (userHashtag → stamp → coupon →
   * user)
   *
   * @throws CustomException 사용자 없음 {@link UserErrorCode#USER_NOT_FOUND}
   * @throws CustomException S3 삭제 실패 {@link UserErrorCode#S3_DELETE_FAILED}
   * @throws CustomException DB 삭제 실패 {@link UserErrorCode#USER_DELETE_FAILED}
   */
  public void deleteCurrentUser() {
    Long userId = SecurityUtil.getCurrentUserId();
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 1) S3 이미지 삭제
    try {
      if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isBlank()) {
        s3Service.deleteByUrl(user.getProfileImageUrl());
      }
      for (Coupon c : couponRepository.findAllByUser_Id(userId)) {
        if (c.getImageUrl() != null && !c.getImageUrl().isBlank()) {
          s3Service.deleteByUrl(c.getImageUrl());
        }
      }
    } catch (RuntimeException e) {
      throw new CustomException(UserErrorCode.S3_DELETE_FAILED);
    }

    // 2) DB 연관 자원 삭제 (자식 → 부모)
    try {
      userHashtagRepository.deleteAllByUser_Id(userId);
      stampRepository.deleteAllByUser_Id(userId);
      couponRepository.deleteAllByUser_Id(userId);
      userRepository.delete(user);
    } catch (RuntimeException e) {
      throw new CustomException(UserErrorCode.USER_DELETE_FAILED);
    }
  }
}
