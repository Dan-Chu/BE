package com.likelion.danchu.domain.stamp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.stamp.entity.Stamp;

@Repository
public interface StampRepository extends JpaRepository<Stamp, Long> {

  Optional<Stamp> findTopByUser_IdAndStore_IdOrderByIdDesc(Long userId, Long storeId);

  List<Stamp> findAllByUser_IdOrderByUpdatedAtDesc(Long userId);

  void deleteAllByUser_Id(Long userId);

  // 해당 가게의 스탬프 전부 삭제 (가게 삭제 시 연관 정리용)
  void deleteByStore_Id(Long storeId);
}
