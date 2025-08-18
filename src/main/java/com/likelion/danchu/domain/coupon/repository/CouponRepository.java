package com.likelion.danchu.domain.coupon.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.coupon.entity.Coupon;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

  // 가장 임박한 쿠폰 → 가장 여유 있는 쿠폰 순서로 조회
  List<Coupon> findAllByExpirationDateGreaterThanEqualOrderByExpirationDateAsc(LocalDate date);

  List<Coupon> findAllByUser_Id(Long userId);

  void deleteAllByUser_Id(Long userId);

  // 해당 가게의 쿠폰 전부 삭제 (가게 삭제 시 연관 정리용)
  void deleteByStore_Id(Long storeId);
}
