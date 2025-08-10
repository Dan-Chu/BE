package com.likelion.danchu.domain.stamp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.stamp.entity.Stamp;

@Repository
public interface StampRepository extends JpaRepository<Stamp, Long> {

  Optional<Stamp> findTopByUserIdAndStoreIdOrderByIdDesc(Long userId, Long storeId);
}
