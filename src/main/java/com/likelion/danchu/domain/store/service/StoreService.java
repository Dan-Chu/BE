package com.likelion.danchu.domain.store.service;

import com.likelion.danchu.domain.coupon.repository.CouponRepository;
import com.likelion.danchu.domain.hashtag.dto.response.HashtagResponse;
import com.likelion.danchu.domain.hashtag.mapper.HashtagMapper;
import com.likelion.danchu.domain.hashtag.repository.HashtagRepository;
import com.likelion.danchu.domain.menu.dto.response.MenuResponse;
import com.likelion.danchu.domain.menu.mapper.MenuMapper;
import com.likelion.danchu.domain.menu.repository.MenuRepository;
import com.likelion.danchu.domain.mission.repository.MissionRepository;
import com.likelion.danchu.domain.stamp.repository.StampRepository;
import com.likelion.danchu.domain.store.dto.request.StoreRequest;
import com.likelion.danchu.domain.store.dto.response.PageableResponse;
import com.likelion.danchu.domain.store.dto.response.StoreDistanceResponse;
import com.likelion.danchu.domain.store.dto.response.StoreListItemResponse;
import com.likelion.danchu.domain.store.dto.response.StoreResponse;
import com.likelion.danchu.domain.store.entity.Store;
import com.likelion.danchu.domain.store.entity.StoreHashtag;
import com.likelion.danchu.domain.store.exception.StoreErrorCode;
import com.likelion.danchu.domain.store.mapper.StoreMapper;
import com.likelion.danchu.domain.store.repository.StoreHashtagRepository;
import com.likelion.danchu.domain.store.repository.StoreRepository;
import com.likelion.danchu.domain.user.repository.UserRepository;
import com.likelion.danchu.global.exception.CustomException;
import com.likelion.danchu.global.util.DistanceUtils;
import com.likelion.danchu.infra.kakao.KakaoLocalClient;
import com.likelion.danchu.infra.s3.entity.PathName;
import com.likelion.danchu.infra.s3.service.S3Service;
import io.micrometer.common.lang.Nullable;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreService {

  private final StoreRepository storeRepository;
  private final StoreMapper storeMapper;
  private final S3Service s3Service;
  private final HashtagRepository hashtagRepository;
  private final StoreHashtagRepository storeHashtagRepository;
  private final HashtagMapper hashtagMapper;
  private final KakaoLocalClient kakaoLocalClient;
  private final MenuRepository menuRepository;
  private final MenuMapper menuMapper;
  private final MissionRepository missionRepository;
  private final UserRepository userRepository;
  private final CouponRepository couponRepository;
  private final StampRepository stampRepository;

  /**
   * 새로운 가게를 생성합니다.
   *
   * @param storeRequest 가게 생성 요청 DTO
   * @param imageFile    업로드할 메인 이미지 파일
   * @return 생성된 가게 정보
   * @throws CustomException 주소 또는 인증 코드가 중복되거나, 이미지 업로드/저장 실패 시 발생
   */
  public StoreResponse createStore(StoreRequest storeRequest, MultipartFile imageFile) {
    // 주소로 가게 중복 체크
    if (storeRepository.existsByAddress(storeRequest.getAddress())) {
      throw new CustomException(StoreErrorCode.STORE_ALREADY_EXISTS);
    }

    // 인증 코드 중복 체크
    if (storeRepository.existsByAuthCode(storeRequest.getAuthCode())) {
      throw new CustomException(StoreErrorCode.STORE_AUTHCODE_DUPLICATED);
    }

    // 이미지 업로드 (PathName.STORE 폴더에 저장)
    final String imageUrl;
    try {
      imageUrl = s3Service.uploadImage(PathName.STORE, imageFile).getImageUrl();
    } catch (Exception e) {
      throw new CustomException(StoreErrorCode.IMAGE_UPLOAD_FAILED);
    }

    // 항상 주소를 좌표 변환
    var coords =
        kakaoLocalClient
            .geocode(storeRequest.getAddress())
            .orElseThrow(() -> new CustomException(StoreErrorCode.GEOCODING_FAILED));
    double lon = coords.lon(); // 경도
    double lat = coords.lat(); // 위도

    // 저장
    try {
      Store store =
          Store.builder()
              .name(storeRequest.getName())
              .address(storeRequest.getAddress())
              .description(storeRequest.getDescription())
              .phoneNumber(storeRequest.getPhoneNumber())
              .openTime(storeRequest.getOpenTime())
              .closeTime(storeRequest.getCloseTime())
              .authCode(storeRequest.getAuthCode())
              .mainImageUrl(imageUrl)
              .stampReward(storeRequest.getStampReward())
              .latitude(lat)
              .longitude(lon)
              .build();

      Store saved = storeRepository.save(store);
      return storeMapper.toResponse(saved, List.of()); // 생성 직후 해시태그 없음
    } catch (Exception e) {
      throw new CustomException(StoreErrorCode.STORE_SAVE_FAILED);
    }
  }

  /**
   * 전체 가게 목록을 페이징 조회합니다. 각 가게의 해시태그를 함께 반환합니다.
   *
   * @param page 페이지 번호 (0부터 시작)
   * @param size 한 페이지에 포함될 가게 수
   * @return 페이징된 가게 목록 응답
   */
  public PageableResponse<StoreListItemResponse> getPaginatedStores(int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size); // 페이지당 3개
    Page<Store> storePage = storeRepository.findAll(pageRequest);
    List<Store> stores = storePage.getContent();

    if (stores.isEmpty()) {
      // 빈 페이지라도 제네릭 타입은 StoreListItemResponse 이어야 함
      Page<StoreListItemResponse> empty = storePage.map(s -> storeMapper.toListItem(s, List.of()));
      return PageableResponse.from(empty);
    }

    // 현재 페이지의 가게 ID들 수집
    List<Long> storeIds = stores.stream().map(Store::getId).toList();

    // 모든 해시태그 연결 한번에 조회
    List<StoreHashtag> relations = storeHashtagRepository.findByStore_IdIn(storeIds);

    // 해시태그 응답 리스트로 매팅
    Map<Long, List<HashtagResponse>> hashtagsByStoreId =
        relations.stream()
            .collect(
                Collectors.groupingBy(
                    storeHashtag -> storeHashtag.getStore().getId(),
                    Collectors.mapping(
                        storeHashtag -> hashtagMapper.toResponse(storeHashtag.getHashtag()),
                        Collectors.toList())));

    // 각 가게 별로 해시태그 포함하여 DTO 변환
    Page<StoreListItemResponse> wrappedPage =
        storePage.map(
            store ->
                storeMapper.toListItem(
                    store,
                    hashtagsByStoreId.getOrDefault(store.getId(), List.of()),
                    List.of() // 메뉴 미포함. 필요 시 주입
                ));

    return PageableResponse.from(wrappedPage);
  }

  /**
   * 검색어(keyword)가 가게 이름에 포함된 가게들을 페이징 조회합니다. 각 가게의 해시태그를 함께 반환합니다.
   *
   * @param keyword 검색 키워드
   * @param page    페이지 번호 (0부터 시작)
   * @param size    한 페이지에 포함될 가게 수
   * @return 페이징된 가게 목록 응답
   * @throws CustomException 검색어가 비어 있는 경우
   */
  public PageableResponse<StoreDistanceResponse> searchStoresByKeyword(
      String keyword, int page, int size, @Nullable Double lat, @Nullable Double lng) {
    if (keyword == null || keyword.trim().isEmpty()) {
      throw new CustomException(StoreErrorCode.EMPTY_KEYWORD);
    }
    Pageable pageable = PageRequest.of(page, size);
    Page<Store> storePage =
        storeRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
    List<Store> stores = storePage.getContent();

    // 현재 페이지 가게들의 해시태그를 한 번에 조회 (N+1 방지)
    Map<Long, List<HashtagResponse>> hashtagsByStoreId =
        stores.isEmpty() ? Map.of() : loadHashtagsByStoreIds(stores);

    /**
     * 검색된 가게를 StoreDistanceResponse 형태로 변환합니다.
     *
     * <p>- 각 가게는 StoreResponse(기본 정보 + 해시태그 포함)로 매핑됩니다.
     *
     * <p>- 요청에 위치 좌표(lat/lng)가 있으면 사용자-가게 간 거리를 계산합니다.
     *
     * <p>- 위치 좌표가 없거나 가게 좌표가 없으면 거리 값은 null로 내려가며,\ 응답 구조(content[i].store / distanceMeters /
     * distanceKm)는 동일하게 유지됩니다.
     */
    Page<StoreDistanceResponse> responsePage =
        storePage.map(
            store -> {
              StoreResponse sr =
                  storeMapper.toResponse(
                      store, hashtagsByStoreId.getOrDefault(store.getId(), List.of()));

              Double meters = null;
              if (lat != null
                  && lng != null
                  && store.getLatitude() != null
                  && store.getLongitude() != null) {
                double m =
                    DistanceUtils.haversineMeters(
                        lat, lng, store.getLatitude(), store.getLongitude());
                meters = DistanceUtils.round1(m); // 소수 1자리
              }

              Double km = (meters == null) ? null : DistanceUtils.round2(meters / 1000d); // 소수 2자리

              return StoreDistanceResponse.builder()
                  .store(sr)
                  .distanceMeters(meters)
                  .distanceKm(km)
                  .build();
            });

    // 좌표가 있으면 현재 페이지 내에서 거리 오름차순 정렬 후 반환
    if (lat != null && lng != null) {
      var sorted =
          responsePage.getContent().stream()
              .sorted(
                  Comparator.comparing(
                      r ->
                          r.getDistanceMeters() == null ? Double.MAX_VALUE : r.getDistanceMeters()))
              .toList();

      PageImpl<StoreDistanceResponse> sortedPage =
          new PageImpl<>(sorted, responsePage.getPageable(), responsePage.getTotalElements());

      return PageableResponse.from(sortedPage);
    }

    // 좌표가 없으면: 기존 순서 그대로 반환
    return PageableResponse.from(responsePage);
  }

  /**
   * 특정 가게의 상세 정보를 조회합니다. 해당 가게의 해시태그와 메뉴 목록을 함께 반환합니다.
   *
   * @param storeId 조회할 가게 ID
   * @return 가게 상세 정보 응답
   * @throws CustomException 가게가 존재하지 않는 경우
   */
  public StoreResponse getStoreDetail(Long storeId) {
    Store store =
        storeRepository
            .findById(storeId)
            .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

    // 가게 해시태그 목록 조회
    List<StoreHashtag> relations = storeHashtagRepository.findByStore_Id(storeId);
    List<HashtagResponse> hashtagResponses =
        relations.stream().map(StoreHashtag::getHashtag).map(hashtagMapper::toResponse).toList();

    // 가게 메뉴 목록 조회
    List<MenuResponse> menuResponses =
        menuRepository.findByStore_IdOrderByIdAsc(storeId).stream()
            .map(menuMapper::toResponse)
            .toList();

    // 해시태그 + 메뉴 포함하여 응답 생성
    return storeMapper.toResponse(store, hashtagResponses, menuResponses);
  }

  /**
   * 현재 위치(:lat, :lng)를 기준으로 거리순으로 정렬된 가게 목록을 페이징 조회합니다.
   *
   * @param lat          사용자 위도(WGS84)
   * @param lng          사용자 경도(WGS84)
   * @param page         0부터 시작하는 페이지 번호
   * @param size         페이지 크기(1 이상)
   * @param radiusMeters 검색 반경(미터). null이면 반경 제한 없음
   * @return 거리 정보가 포함된 페이징 응답
   */
  public PageableResponse<StoreDistanceResponse> getNearbyStores(
      double lat, double lng, int page, int size, Double radiusMeters) {

    PageRequest pageable = PageRequest.of(page, size);
    Page<StoreRepository.StoreWithDistanceProjection> projPage =
        storeRepository.findNearby(lat, lng, radiusMeters, pageable);

    // 현재 페이지 가게들의 해시태그를 한 번에 조회
    Map<Long, List<HashtagResponse>> hashtagsByStoreId =
        projPage.getContent().isEmpty()
            ? Map.of()
            : loadHashtagsByStoreIds(
                projPage.getContent().stream()
                    .map(p -> Store.builder().id(p.getId()).build())
                    .toList());

    Page<StoreDistanceResponse> mapped =
        projPage.map(
            p -> {
              // Projection -> Store -> StoreResponse
              Store store =
                  Store.builder()
                      .id(p.getId())
                      .name(p.getName())
                      .address(p.getAddress())
                      .description(p.getDescription())
                      .phoneNumber(p.getPhone_Number())
                      .openTime(p.getOpen_Time())
                      .closeTime(p.getClose_Time())
                      .mainImageUrl(p.getMain_Image_Url())
                      .latitude(p.getLatitude())
                      .longitude(p.getLongitude())
                      .authCode("") // 응답 비노출
                      .build();

              StoreResponse storeRes =
                  storeMapper.toResponse(
                      store, hashtagsByStoreId.getOrDefault(store.getId(), List.of()));

              double meters = p.getDistance_m();
              return StoreDistanceResponse.builder()
                  .store(storeRes)
                  .distanceMeters(DistanceUtils.round1(meters)) // 소수 1자리
                  .distanceKm(DistanceUtils.round2(meters / 1000d)) // 소수 2자리
                  .build();
            });

    return PageableResponse.from(mapped);
  }

  /**
   * 현재 페이지에 포함된 가게들의 해시태그를 한 번에 로드해 N+1 쿼리를 방지합니다. *
   *
   * @param stores 해시태그를 조회할 가게 목록(비-null)
   * @return 가게 ID -> 해시태그 응답 리스트 매핑
   */
  private Map<Long, List<HashtagResponse>> loadHashtagsByStoreIds(List<Store> stores) {
    List<Long> storeIds = stores.stream().map(Store::getId).toList();
    List<StoreHashtag> relations =
        storeIds.isEmpty() ? List.of() : storeHashtagRepository.findByStore_IdIn(storeIds);

    return relations.stream()
        .collect(
            Collectors.groupingBy(
                r -> r.getStore().getId(),
                Collectors.mapping(
                    r -> hashtagMapper.toResponse(r.getHashtag()), Collectors.toList())));
  }

  /**
   * 가게 삭제
   *
   * <p>S3 같은 외부 시스템 실패 때문에 핵심 트랜잭션(DB 삭제)을 롤백시키지 않기 위해 이미지 삭제는 실패해도 진행
   */
  public void deleteStore(Long storeId) {
    // 가게 조회
    Store store =
        storeRepository
            .findById(storeId)
            .orElseThrow(() -> new CustomException(StoreErrorCode.STORE_NOT_FOUND));

    // 해당 스토어의 미션 ID들 수집
    List<Long> missionIds = missionRepository.findIdsByStoreId(storeId);

    // 미션 리워드 이미지 S3 삭제
    try {
      if (!missionIds.isEmpty()) {
        List<String> rewardUrls = missionRepository.findRewardImageUrlsByIds(missionIds);
        for (String url : rewardUrls) {
          if (url != null && !url.isBlank()) {
            s3Service.deleteByUrl(url);
          }
        }
      }
    } catch (Exception ignore) {
      // 이미지 삭제 실패는 전체 삭제를 막지 않음
    }

    // 미션 삭제
    missionRepository.deleteByStore_Id(storeId);

    // 쿠폰, 스탬프, 메뉴, 해시태그 관계 삭제
    couponRepository.deleteByStore_Id(storeId);
    stampRepository.deleteByStore_Id(storeId);
    menuRepository.deleteByStore_Id(storeId);
    storeHashtagRepository.deleteByStore_Id(storeId);

    // 가게 메인 이미지 S3 삭제
    try {
      String url = store.getMainImageUrl();
      if (url != null && !url.isBlank()) {
        s3Service.deleteByUrl(url);
      }
    } catch (Exception ignore) {
      // 이미지 삭제 실패는 전체 삭제를 막지 않음
    }

    storeRepository.delete(store);
  }
}
