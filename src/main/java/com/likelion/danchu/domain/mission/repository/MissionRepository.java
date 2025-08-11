package com.likelion.danchu.domain.mission.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.mission.entity.Mission;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {

  // 지정한 가게, 날짜, 제목이 모두 일치하는 미션이 존재하는지 확인
  boolean existsByStoreIdAndDateAndTitle(Long storeId, LocalDate date, String title);

  // 오늘 날짜 미션 중 해당 유저가 아직 완료 안한 미션만
  @Query(
      """
        select m
        from Mission m
        where m.date = :today
          and not exists (
            select 1
            from User u
            where u.id = :userId
              and m.id member of u.completedMissionIds
          )
      """)
  List<Mission> findTodayNotCompleted(
      @Param("today") LocalDate today, @Param("userId") Long userId);
}
