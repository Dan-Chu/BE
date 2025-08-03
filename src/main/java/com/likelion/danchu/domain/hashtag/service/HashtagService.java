package com.likelion.danchu.domain.hashtag.service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.likelion.danchu.domain.hashtag.dto.request.HashtagRequest;
import com.likelion.danchu.domain.hashtag.dto.response.HashtagResponse;
import com.likelion.danchu.domain.hashtag.entity.Hashtag;
import com.likelion.danchu.domain.hashtag.exception.HashtagErrorCode;
import com.likelion.danchu.domain.hashtag.mapper.HashtagMapper;
import com.likelion.danchu.domain.hashtag.repository.HashtagRepository;
import com.likelion.danchu.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class HashtagService {

  private final HashtagRepository hashtagRepository;
  private final HashtagMapper hashtagMapper;

  public HashtagResponse createHashtag(HashtagRequest request) {
    // 입력값에서 모든 공백 제거
    String rawName = request.getName().trim().replaceAll("\\s+", "");
    // '#' 없이 입력한 경우 자동으로 앞에 '#'을 붙임
    String formattedName =
        request.getName().startsWith("#") ? request.getName() : "#" + request.getName();

    // 중복 해시태그 검사
    if (hashtagRepository.findByName(request.getName()).isPresent()) {
      throw new CustomException(HashtagErrorCode.HASHTAG_ALREADY_EXISTS);
    }

    Hashtag hashtag = Hashtag.builder().name(request.getName()).build();
    Hashtag savedHashtag = hashtagRepository.save(hashtag);

    return hashtagMapper.toResponse(savedHashtag);
  }
}
