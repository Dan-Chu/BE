package com.likelion.danchu.domain.store.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.store.entity.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

  // 주소로 가게 중복 확인
  boolean existsByAddress(String address);

  // 인증번호 중복 확인
  boolean existsByAuthCode(String authCode);

  // 가게 이름 검색
  List<Store> findByNameContainingIgnoreCase(String keyword);
}
