package com.likelion.danchu.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.user.entity.UserHashtag;

@Repository
public interface UserHashtagRepository extends JpaRepository<UserHashtag, Long> {}
