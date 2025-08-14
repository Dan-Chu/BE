package com.likelion.danchu.domain.store.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
@Table(name = "store")
public class Store extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false, length = 10)
  private String name;

  @Column(name = "address", nullable = false, length = 50)
  private String address;

  @Column(name = "description", nullable = false, length = 200)
  private String description;

  @Column(name = "phone_number", nullable = false)
  private String phoneNumber;

  @Column(name = "open_time", nullable = false)
  private String openTime;

  @Column(name = "close_time", nullable = false)
  private String closeTime;

  @Column(name = "auth_code", nullable = false)
  private String authCode;

  @Column(name = "main_image_url", nullable = false)
  private String mainImageUrl;

  @Column(name = "stamp_reward", nullable = true)
  private String stampReward;

  @Column(name = "latitude", nullable = false) // 위도
  private Double latitude;

  @Column(name = "longitude", nullable = false) // 경도
  private Double longitude;
}
