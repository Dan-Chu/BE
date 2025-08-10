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
}
