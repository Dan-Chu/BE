package com.likelion.danchu.domain.mission.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.mission.entity.Mission;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {

  // 지정한 가게, 날짜, 제목이 모두 일치하는 미션이 존재하는지 확인
  boolean existsByStoreIdAndDateAndTitle(Long storeId, LocalDate date, String title);
}
