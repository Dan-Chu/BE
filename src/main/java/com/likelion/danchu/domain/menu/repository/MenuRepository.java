package com.likelion.danchu.domain.menu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.menu.entity.Menu;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

  // 가게 ID와 메뉴 이름으로 중복 확인
  boolean existsByStore_IdAndName(Long storeId, String name);
}
