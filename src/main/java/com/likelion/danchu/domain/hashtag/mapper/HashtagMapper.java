package com.likelion.danchu.domain.hashtag.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.likelion.danchu.domain.hashtag.dto.response.HashtagResponse;
import com.likelion.danchu.domain.hashtag.entity.Hashtag;

@Component
public class HashtagMapper {

  public HashtagResponse toResponse(Hashtag hashtag) {
    return HashtagResponse.builder().id(hashtag.getId()).name(hashtag.getName()).build();
  }

  public List<HashtagResponse> toResponseList(List<Hashtag> hashtags) {
    return hashtags.stream().map(this::toResponse).collect(Collectors.toList());
  }
}
