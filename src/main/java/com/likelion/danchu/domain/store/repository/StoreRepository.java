package com.likelion.danchu.domain.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.likelion.danchu.domain.store.entity.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

  // 주소로 가게 중복 확인
  boolean existsByAddress(String address);

  // 인증번호 중복 확인
  boolean existsByAuthCode(String authCode);

  // 가게 이름 검색
  Page<Store> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

  // 해시태그 기반 가게 목록 조회
  Page<Store> findDistinctByIdIn(List<Long> ids, Pageable pageable);

  Optional<Store> findByAuthCode(String authCode);

  /**
   * 네이티브 쿼리 결과의 컬럼명과 동일한 이름의 getter 를 가진 Projection. - snake_case 컬럼명(예: phone_number) ↔
   * getPhone_Number() - 계산 컬럼 distance_m ↔ getDistance_m()
   */
  interface StoreWithDistanceProjection {

    Long getId();

    String getName();

    String getAddress();

    String getDescription();

    String getPhone_Number();

    String getOpen_Time();

    String getClose_Time();

    String getMain_Image_Url();

    Double getLatitude();

    Double getLongitude();

    Double getDistance_m(); // alias와 동일해야 함
  }

  /**
   * 현재 위치(:lat, :lng) 기준으로 거리(m) 계산 후 가까운 순 정렬합니다.
   *
   * <ul>
   *   <li>6,371,000</b>: 지구 반지름(m)
   *   <li>LEAST(1, ...)</code>로 acos 인자 범위 보정(> 1 방지)
   *   <li>:radius</code> 지정 시 해당 반경(m) 내로 필터링
   * </ul>
   */
  @Query(
      value =
          """
              SELECT
                s.id,
                s.name,
                s.address,
                s.description,
                s.phone_number,
                s.open_time,
                s.close_time,
                s.main_image_url,
                s.latitude,
                s.longitude,
                (6371000 * ACOS(LEAST(1,
                   COS(RADIANS(:lat)) * COS(RADIANS(s.latitude)) * COS(RADIANS(s.longitude) - RADIANS(:lng))
                   + SIN(RADIANS(:lat)) * SIN(RADIANS(s.latitude))
                ))) AS distance_m
              FROM store s
              WHERE (:radius IS NULL OR
                    (6371000 * ACOS(LEAST(1,
                      COS(RADIANS(:lat)) * COS(RADIANS(s.latitude)) * COS(RADIANS(s.longitude) - RADIANS(:lng))
                      + SIN(RADIANS(:lat)) * SIN(RADIANS(s.latitude))
                    ))) <= :radius)
              ORDER BY distance_m ASC
              """,
      countQuery =
          """
              SELECT COUNT(*) FROM store s
              WHERE (:radius IS NULL OR
                    (6371000 * ACOS(LEAST(1,
                      COS(RADIANS(:lat)) * COS(RADIANS(s.latitude)) * COS(RADIANS(s.longitude) - RADIANS(:lng))
                      + SIN(RADIANS(:lat)) * SIN(RADIANS(s.latitude))
                    ))) <= :radius)
              """,
      nativeQuery = true)
  Page<StoreWithDistanceProjection> findNearby(
      @Param("lat") double lat,
      @Param("lng") double lng,
      @Param("radius") Double radius,
      Pageable pageable);
}
