package com.likelion.danchu.domain.hashtag.mapper;

import org.springframework.stereotype.Component;

import com.likelion.danchu.domain.hashtag.dto.response.HashtagResponse;
import com.likelion.danchu.domain.hashtag.entity.Hashtag;

@Component
public class HashtagMapper {

  public HashtagResponse toResponse(Hashtag hashtag) {
    return HashtagResponse.builder().id(hashtag.getId()).name(hashtag.getName()).build();
  }
}
