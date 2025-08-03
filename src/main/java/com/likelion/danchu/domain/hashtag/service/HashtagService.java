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
    String formattedName = request.toFormattedName(); // 가공된 이름 추출

    if (hashtagRepository.findByName(formattedName).isPresent()) {
      throw new CustomException(HashtagErrorCode.HASHTAG_ALREADY_EXISTS);
    }

    if (formattedName.length() < 2 || formattedName.length() > 11) {
      throw new CustomException(HashtagErrorCode.HASHTAG_LENGTH_INVALID);
    }

    Hashtag hashtag = Hashtag.builder().name(formattedName).build();
    Hashtag savedHashtag = hashtagRepository.save(hashtag);

    return hashtagMapper.toResponse(savedHashtag);
  }
}
