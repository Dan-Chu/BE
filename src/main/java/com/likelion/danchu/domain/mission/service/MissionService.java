package com.likelion.danchu.domain.mission.service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.likelion.danchu.global.security.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionService {

  private final SecurityUtil securityUtil;
}
