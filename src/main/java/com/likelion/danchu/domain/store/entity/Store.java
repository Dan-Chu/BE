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

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "address", nullable = false)
  private String address;

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "phone_number", nullable = false)
  private String phoneNumber;

  @Column(name = "opening_hour", nullable = false)
  private String openingHour;

  @Column(name = "auth_code", nullable = false)
  private String authCode;

  @Column(name = "main_image_url", nullable = false)
  private String mainImageUrl;

  @Column(name = "menu_image_url", nullable = false)
  private String menuImageUrl;
}
