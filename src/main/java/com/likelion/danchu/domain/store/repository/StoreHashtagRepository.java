package com.likelion.danchu.domain.store.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.hashtag.entity.Hashtag;
import com.likelion.danchu.domain.store.entity.Store;
import com.likelion.danchu.domain.store.entity.StoreHashtag;

@Repository
public interface StoreHashtagRepository extends JpaRepository<StoreHashtag, Long> {

  // 특정 가게에 특정 해시태그가 이미 존재하는지 확인
  boolean existsByStoreAndHashtag(Store store, Hashtag hashtag);

  // 여러 해시태그 중 하나라도 연결된 StoreHashtag 목록을 조회
  List<StoreHashtag> findByHashtagIn(List<Hashtag> hashtags);

  // 가게 목록 페이징 조회 시 각 가게의 해시태그 모두 조회
  List<StoreHashtag> findByStore_IdIn(List<Long> storeIds);

  // 특정 가게 상세 조회 시 해시태그 모두 조회
  List<StoreHashtag> findByStore_Id(Long storeId);

  @Query(
      """
            select sh from StoreHashtag sh
              join fetch sh.store s
              join fetch sh.hashtag h
          """)
  List<StoreHashtag> findAllWithStoreAndHashtag();

  // 해당 가게에 등록된 해시태그 개수 반환
  long countByStore_Id(Long storeId);
}
