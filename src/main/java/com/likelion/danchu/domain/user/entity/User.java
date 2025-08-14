package com.likelion.danchu.domain.user.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.likelion.danchu.global.common.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "users")
public class User extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "nickname", nullable = false, unique = true)
  private String nickname;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @JsonIgnore
  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "profile_image_url", nullable = true)
  private String profileImageUrl;

  // 완료한 미션 ID 리스트 조인테이블로 저장
  @Builder.Default
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
      name = "user_completed_mission",
      joinColumns = @JoinColumn(name = "user_id"),
      uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "mission_id"}))
  @Column(name = "mission_id", nullable = false)
  private List<Long> completedMissionIds = new ArrayList<>();

  // 중복 방지하면서 missionId 추가 (추가되면 true)
  public boolean addCompletedMissionId(Long missionId) {
    if (!this.completedMissionIds.contains(missionId)) {
      this.completedMissionIds.add(missionId);
      return true;
    }
    return false;
  }

  public void updateInfo(String nickname, String email, String imageUrl) {
    this.nickname = nickname;
    this.email = email;
    this.profileImageUrl = imageUrl;
  }
}
