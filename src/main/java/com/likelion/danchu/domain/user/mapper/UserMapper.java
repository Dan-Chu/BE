package com.likelion.danchu.domain.user.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.likelion.danchu.domain.hashtag.dto.response.HashtagResponse;
import com.likelion.danchu.domain.hashtag.entity.Hashtag;
import com.likelion.danchu.domain.user.dto.response.UserResponse;
import com.likelion.danchu.domain.user.entity.User;

@Component
public class UserMapper {
  /**
   * 컨트롤러로부터 전달받은 파라미터를 {@link User} 엔티티로 변환
   *
   * @param nickname 닉네임(중복 검증 완료)
   * @param email 이메일(중복 검증 완료)
   * @param encodedPassword bcrypt 등으로 인코딩된 비밀번호
   * @param imageUrl S3 업로드 결과 URL (nullable)
   * @return persistence 대상 {@link User}
   */
  public User toEntity(String nickname, String email, String encodedPassword, String imageUrl) {

    return User.builder()
        .nickname(nickname)
        .email(email)
        .password(encodedPassword)
        .profileImageUrl(imageUrl)
        .build();
  }

  /**
   * 회원 정보를 응답 DTO로 변환하는 메서드
   *
   * @param user 회원 엔티티
   * @param completedMission 완료한 미션 개수
   * @param hashtags 관심 해시태그 목록 (nullable 허용)
   * @return {@link UserResponse} 응답 객체
   */
  public UserResponse toResponse(User user, long completedMission, List<Hashtag> hashtags) {
    List<HashtagResponse> hashtagResponses =
        hashtags == null
            ? List.of()
            : hashtags.stream().map(h -> new HashtagResponse(h.getId(), h.getName())).toList();

    return UserResponse.builder()
        .id(user.getId())
        .nickname(user.getNickname())
        .email(user.getEmail())
        .completedMission(completedMission)
        .profileImageUrl(user.getProfileImageUrl())
        .hashtags(hashtagResponses)
        .build();
  }
}
