package com.likelion.danchu.external.kakao;

import java.util.List;

public record KakaoLocalAddressResponse(List<Document> documents) {

  // x=경도(lon), y=위도(lat)
  public record Document(String x, String y) {}
}
