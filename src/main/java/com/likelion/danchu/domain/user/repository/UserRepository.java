package com.likelion.danchu.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.likelion.danchu.domain.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);

  Boolean existsByNickname(String nickname);

  @Query(
      "select count(mid) "
          + "from User u "
          + "join u.completedMissionIds mid "
          + "where u.id = :userId")
  long countCompletedMissions(@Param("userId") Long userId);

  // 조인 테이블 직접 삭제 (엔티티가 아니어서 NativeQuery 필요)
  // DML 실행이므로 @Modifying
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query(
      value = "DELETE FROM user_completed_mission WHERE mission_id IN (:missionIds)",
      nativeQuery = true)
  void deleteByMissionIdIn(@Param("missionIds") List<Long> missionIds);
}
