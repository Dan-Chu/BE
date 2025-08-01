package com.likelion.danchu.domain.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.coupon.entity.Coupon;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {}
