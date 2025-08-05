package com.likelion.danchu.domain.store.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

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
        .build();
  }

  // Store Entity를 StoreResponse DTO로 변환
  public StoreResponse toResponse(Store store) {
    return StoreResponse.builder()
        .id(store.getId())
        .name(store.getName())
        .address(store.getAddress())
        .description(store.getDescription())
        .phoneNumber(store.getPhoneNumber())
        .openTime(store.getOpenTime())
        .closeTime(store.getCloseTime())
        .mainImageUrl(store.getMainImageUrl())
        .build();
  }

  // Store Entity List → StoreResponse DTO List
  public List<StoreResponse> toResponseList(List<Store> stores) {
    return stores.stream().map(this::toResponse).toList();
  }
}
