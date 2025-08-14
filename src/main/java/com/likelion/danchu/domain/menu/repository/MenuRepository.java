package com.likelion.danchu.domain.menu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.menu.entity.Menu;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

  // 가게 ID와 메뉴 이름으로 중복 확인
  boolean existsByStore_IdAndName(Long storeId, String name);

  // 특정 가게(storeId)의 메뉴 전체를 ID 오름차순으로 조회
  List<Menu> findByStore_IdOrderByIdAsc(Long storeId);

  // 여러 가게(storeIds)의 메뉴를 한 번에 조회 (N+1 방지용)
  List<Menu> findByStore_IdInOrderByIdAsc(List<Long> storeIds);
}
