package com.likelion.danchu.domain.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.user.entity.User;
import com.likelion.danchu.domain.user.entity.UserHashtag;

@Repository
public interface UserHashtagRepository extends JpaRepository<UserHashtag, Long> {
  void deleteByUser(User user);

  List<UserHashtag> findAllByUser(User user);

  void deleteAllByUser_Id(Long userId);
}
