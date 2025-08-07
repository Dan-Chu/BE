package com.likelion.danchu.domain.hashtag.service;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.likelion.danchu.domain.hashtag.dto.request.HashtagRequest;
import com.likelion.danchu.domain.hashtag.dto.response.HashtagResponse;
import com.likelion.danchu.domain.hashtag.entity.Hashtag;
import com.likelion.danchu.domain.hashtag.exception.HashtagErrorCode;
import com.likelion.danchu.domain.hashtag.mapper.HashtagMapper;
import com.likelion.danchu.domain.hashtag.repository.HashtagRepository;
import com.likelion.danchu.domain.store.entity.Store;
import com.likelion.danchu.domain.store.entity.StoreHashtag;
import com.likelion.danchu.domain.store.exception.StoreErrorCode;
import com.likelion.danchu.domain.store.repository.StoreHashtagRepository;
import com.likelion.danchu.domain.store.repository.StoreRepository;
import com.likelion.danchu.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class HashtagService {

  private final HashtagRepository hashtagRepository;
  private final HashtagMapper hashtagMapper;
  private final StoreRepository storeRepository;
  private final StoreHashtagRepository storeHashtagRepository;

  public HashtagResponse createHashtagForStore(Long storeId, HashtagRequest request) {
    String formattedName = request.toFormattedName(); // 가공된 이름 추출

    if (formattedName.length() < 2 || formattedName.length() > 11) {
      throw new CustomException(HashtagErrorCode.HASHTAG_LENGTH_INVALID);
    }

    // 기존 해시태그가 있다면 재사용, 없으면 새로 생성
    Hashtag hashtag =
        hashtagRepository
            .findByName(formattedName)
            .orElseGet(() -> hashtagRepository.save(Hashtag.builder().name(formattedName).build()));

    // 가게 조회
    Store store =
        storeRepository
            .findById(storeId)
            .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

    // 이미 해당 가게에 연결된 해시태그인지 확인
    if (storeHashtagRepository.existsByStoreAndHashtag(store, hashtag)) {
      throw new CustomException(HashtagErrorCode.HASHTAG_ALREADY_EXISTS);
    }

    StoreHashtag storeHashtag = StoreHashtag.builder().store(store).hashtag(hashtag).build();
    storeHashtagRepository.save(storeHashtag);

    return hashtagMapper.toResponse(hashtag);
  }

  public List<HashtagResponse> getAllHashtags() {
    List<Hashtag> hashtags = hashtagRepository.findAll();
    return hashtagMapper.toResponseList(hashtags);
  }
}
