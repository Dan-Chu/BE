package com.likelion.danchu.domain.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.hashtag.entity.Hashtag;
import com.likelion.danchu.domain.store.entity.Store;
import com.likelion.danchu.domain.store.entity.StoreHashtag;

@Repository
public interface StoreHashtagRepository extends JpaRepository<StoreHashtag, Long> {

  // 특정 가게에 특정 해시태그가 이미 존재하는지 확인
  boolean existsByStoreAndHashtag(Store store, Hashtag hashtag);
}
