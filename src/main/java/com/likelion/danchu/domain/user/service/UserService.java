package com.likelion.danchu.domain.user.service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.likelion.danchu.global.security.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  private final SecurityUtil securityUtil;
}
