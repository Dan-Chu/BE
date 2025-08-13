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
  boolean existsByStore_IdAndDateAndTitle(Long storeId, LocalDate date, String title);

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

  // 가장 많이 완료된 오늘의 미션 1건의 ID
  @Query(
      value =
          """
              SELECT m.id
              FROM mission m
              JOIN user_completed_mission ucm ON ucm.mission_id = m.id
              WHERE m.date = :today
              GROUP BY m.id
              ORDER BY
                  COUNT(DISTINCT ucm.user_id) DESC,  -- 1순위: 완료한 "유저 수"가 많은 순
                  m.date DESC                        -- 2순위: 미션 날짜가 최신인 순
              LIMIT 1;                               -- 최종적으로 위 조건에 맞는 1건만 가져오기
              """,
      nativeQuery = true)
  Long findMostCompletedMissionId(@Param("today") java.time.LocalDate today);

  // 동일 가게/제목/날짜 조합과 같은 다른 미션 존재 여부 확인(자기 자신 id 제외) — 날짜 변경 시 중복 검사용
  boolean existsByStore_IdAndDateAndTitleAndIdNot(
      Long storeId, LocalDate date, String title, Long id);

  // 특정 미션의 완료 이력(조인 테이블) 건수를 반환 (0보다 크면 완료 이력 존재)
  @Query(
      value = "SELECT COUNT(*) FROM user_completed_mission WHERE mission_id = :missionId",
      nativeQuery = true)
  long countCompletionsByMissionId(@Param("missionId") Long missionId);
}
