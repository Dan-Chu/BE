package com.likelion.danchu.domain.coupon.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.likelion.danchu.domain.coupon.dto.response.CouponResponse;
import com.likelion.danchu.domain.coupon.entity.Coupon;
import com.likelion.danchu.domain.store.entity.Store;
import com.likelion.danchu.domain.user.entity.User;

@Component
public class CouponMapper {

  public Coupon toEntity(User user, Store store, String reward, String imageUrl) {
    return Coupon.builder().user(user).store(store).reward(reward).imageUrl(imageUrl).build();
  }

  public CouponResponse toResponse(Coupon coupon) {
    if (coupon == null) return null;

    return CouponResponse.builder()
        .id(coupon.getId())
        .userId(coupon.getUser().getId())
        .storeId(coupon.getStore().getId())
        .reward(coupon.getReward())
        .imageUrl(coupon.getImageUrl())
        .build();
  }

  public List<CouponResponse> toResponseList(List<Coupon> coupons) {
    return coupons.stream().map(this::toResponse).collect(Collectors.toList());
  }
}
