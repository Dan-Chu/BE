package com.likelion.danchu.domain.mission.mapper;

import org.springframework.stereotype.Component;

import com.likelion.danchu.domain.mission.dto.request.MissionRequest;
import com.likelion.danchu.domain.mission.dto.response.MissionResponse;
import com.likelion.danchu.domain.mission.entity.Mission;
import com.likelion.danchu.domain.store.entity.Store;

@Component
public class MissionMapper {

  public Mission toEntity(Store store, MissionRequest missionRequest, String rewardImageUrl) {
    return Mission.builder()
        .store(store)
        .title(missionRequest.getTitle())
        .description(missionRequest.getDescription())
        .reward(missionRequest.getReward())
        .date(missionRequest.getDate())
        .rewardImageUrl(rewardImageUrl)
        .build();
  }

  public MissionResponse toResponse(Mission mission) {
    return MissionResponse.builder()
        .id(mission.getId())
        .storeId(mission.getStore().getId())
        .title(mission.getTitle())
        .description(mission.getDescription())
        .reward(mission.getReward())
        .date(mission.getDate())
        .rewardImageUrl(mission.getRewardImageUrl())
        .build();
  }
}
