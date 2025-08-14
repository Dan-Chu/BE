package com.likelion.danchu.domain.hashtag.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.hashtag.entity.Hashtag;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

  // 해시태그 이름으로 단일 해시태그를 조회
  Optional<Hashtag> findByName(String name);

  // 여러 해시태그 이름에 해당하는 해시태그 목록을 조회
  List<Hashtag> findByNameIn(List<String> names);
}
