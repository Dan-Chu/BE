package com.likelion.danchu.domain.store.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class StoreHashtagService {

  private final HashtagRepository hashtagRepository;
  private final HashtagMapper hashtagMapper;
  private final StoreRepository storeRepository;
  private final StoreHashtagRepository storeHashtagRepository;

  /**
   * 특정 가게에 해시태그를 생성/연결하는 서비스 메서드
   *
   * @param storeId 해시태그를 등록할 대상 가게 ID
   * @param request 해시태그 생성 요청 DTO
   * @return 생성 또는 재사용된 해시태그 응답 DTO
   * @throws CustomException 해시태그 길이가 유효하지 않거나, 가게를 찾을 수 없거나, 이미 해당 가게에 동일한 해시태그가 연결된 경우
   */
  public HashtagResponse createHashtagForStore(Long storeId, HashtagRequest request) {
    String name = request.toFormattedName();
    if (name.length() < 2 || name.length() > 11) {
      throw new CustomException(HashtagErrorCode.HASHTAG_LENGTH_INVALID);
    }

    // 이미 존재하는 해시태그면 재사용, 없으면 새로 생성
    Hashtag hashtag =
        hashtagRepository
            .findByName(name)
            .orElseGet(() -> hashtagRepository.save(Hashtag.builder().name(name).build()));

    // storeId로 가게 조회
    Store store =
        storeRepository
            .findById(storeId)
            .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

    // 해당 가게에 이미 동일 해시태그가 연결되어 있는지 확인
    if (storeHashtagRepository.existsByStoreAndHashtag(store, hashtag)) {
      throw new CustomException(HashtagErrorCode.HASHTAG_ALREADY_EXISTS);
    }

    storeHashtagRepository.save(StoreHashtag.builder().store(store).hashtag(hashtag).build());

    return hashtagMapper.toResponse(hashtag);
  }
}
