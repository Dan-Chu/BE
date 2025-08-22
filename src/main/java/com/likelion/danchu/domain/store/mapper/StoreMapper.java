package com.likelion.danchu.domain.store.mapper;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.likelion.danchu.domain.hashtag.dto.response.HashtagResponse;
import com.likelion.danchu.domain.menu.dto.response.MenuResponse;
import com.likelion.danchu.domain.store.dto.request.StoreRequest;
import com.likelion.danchu.domain.store.dto.response.StoreResponse;
import com.likelion.danchu.domain.store.entity.Store;

@Component
public class StoreMapper {

  // StoreRequest + 업로드된 이미지 URL로 Store Entity 생성
  public Store toEntity(StoreRequest storeRequest, String imageUrl) {
    return Store.builder()
        .name(storeRequest.getName())
        .address(storeRequest.getAddress())
        .description(storeRequest.getDescription())
        .phoneNumber(storeRequest.getPhoneNumber())
        .openTime(storeRequest.getOpenTime())
        .closeTime(storeRequest.getCloseTime())
        .authCode(storeRequest.getAuthCode())
        .mainImageUrl(imageUrl)
        .stampReward(storeRequest.getStampReward())
        .build();
  }

  public StoreResponse toResponse(
      Store store, List<HashtagResponse> hashtags, List<MenuResponse> menus) {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    LocalTime open = LocalTime.parse(store.getOpenTime(), formatter);
    LocalTime close = LocalTime.parse(store.getCloseTime(), formatter);
    LocalTime now = LocalTime.now(ZoneId.of("Asia/Seoul"));

    boolean isOpen;
    if (open.equals(close)) {
      isOpen = true; // 24시간 영업
    } else if (open.isBefore(close)) {
      // 일반 영업
      isOpen = !now.isBefore(open) && now.isBefore(close);
    } else {
      // 자정을 넘기는 영업
      isOpen = !now.isBefore(open) || now.isBefore(close);
    }

    return StoreResponse.builder()
        .id(store.getId())
        .name(store.getName())
        .address(store.getAddress())
        .description(store.getDescription())
        .phoneNumber(store.getPhoneNumber())
        .openTime(store.getOpenTime())
        .closeTime(store.getCloseTime())
        .authCode(store.getAuthCode())
        .mainImageUrl(store.getMainImageUrl())
        .hashtags(hashtags != null ? hashtags : List.of()) // null 방어
        .isOpen(isOpen)
        .menus(menus != null ? menus : List.of())
        .build();
  }

  public StoreResponse toResponse(Store store, List<HashtagResponse> hashtags) {
    return toResponse(store, hashtags, List.of());
  }

  public StoreResponse toResponse(Store store) {
    return toResponse(store, List.of(), List.of());
  }
}
