package com.likelion.danchu.domain.mission.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.likelion.danchu.domain.store.entity.Store;
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
@Table(name = "mission")
public class Mission extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "store_id", nullable = true)
  private Store store;

  @Column(name = "title", nullable = false, length = 50)
  private String title;

  @Column(name = "description", nullable = false, length = 100)
  private String description;

  @Column(name = "reward", nullable = false, length = 100)
  private String reward;

  @Column(name = "date", nullable = false)
  private LocalDate date;

  @Column(name = "reward_image_url", nullable = true)
  private String rewardImageUrl;

  public void changeDate(LocalDate newDate) {
    this.date = newDate;
  }
}
