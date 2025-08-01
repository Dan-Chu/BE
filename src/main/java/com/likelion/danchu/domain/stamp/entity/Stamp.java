package com.likelion.danchu.domain.stamp.entity;

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
import com.likelion.danchu.domain.user.entity.User;
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
@Table(name = "stamp")
public class Stamp extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "store_id", nullable = false)
  private Store store;

  @Builder.Default
  @Column(name = "count", nullable = false)
  private int count = 1;

  @Column(name = "reward", nullable = false)
  private String reward;

  @Builder.Default
  @Column(name = "is_used", nullable = false)
  private boolean isUsed = false;

  public void useStamp() {
    isUsed = true;
  }

  public void unuseStamp() {
    isUsed = false;
  }

  public void increaseCount() {
    count++;
  }
}
