package com.likelion.danchu.domain.stamp.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.likelion.danchu.domain.stamp.dto.response.StampResponse;
import com.likelion.danchu.domain.stamp.entity.Stamp;
import com.likelion.danchu.domain.store.entity.Store;
import com.likelion.danchu.domain.user.entity.User;

@Component
public class StampMapper {

  public Stamp toEntity(User user, Store store) {
    return Stamp.builder().user(user).store(store).reward(store.getStampReward()).build();
  }

  public StampResponse toResponse(Stamp stamp) {
    return StampResponse.builder()
        .id(stamp.getId())
        .storeName(stamp.getStore().getName())
        .reward(stamp.getReward())
        .currentCount(stamp.getCurrentCount())
        .cardNum(stamp.getCardNum())
        .status(stamp.getStatus())
        .nickname(stamp.getUser().getNickname())
        .authCode(stamp.getStore().getAuthCode())
        .build();
  }

  public List<StampResponse> toResponseList(List<Stamp> stamps) {
    return stamps.stream().map(this::toResponse).collect(Collectors.toList());
  }
}
