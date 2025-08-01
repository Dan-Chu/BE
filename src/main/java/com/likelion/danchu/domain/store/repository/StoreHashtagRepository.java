package com.likelion.danchu.domain.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.store.entity.StoreHashtag;

@Repository
public interface StoreHashtagRepository extends JpaRepository<StoreHashtag, Long> {}
