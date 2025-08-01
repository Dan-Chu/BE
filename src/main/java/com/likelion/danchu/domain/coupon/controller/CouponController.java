package com.likelion.danchu.domain.coupon.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
@Tag(name = "Coupon", description = "Coupon 관련 API")
public class CouponController {}
