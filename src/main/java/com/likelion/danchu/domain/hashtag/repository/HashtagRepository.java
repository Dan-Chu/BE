package com.likelion.danchu.domain.hashtag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.hashtag.entity.Hashtag;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {}
