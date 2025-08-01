package com.likelion.danchu.domain.hashtag.service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.likelion.danchu.global.security.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class HashtagService {

  private final SecurityUtil securityUtil;
}
