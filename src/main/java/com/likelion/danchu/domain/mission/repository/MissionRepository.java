package com.likelion.danchu.domain.mission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.mission.entity.Mission;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {}
