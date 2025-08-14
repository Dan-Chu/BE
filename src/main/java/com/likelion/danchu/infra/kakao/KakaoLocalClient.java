package com.likelion.danchu.infra.kakao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.likelion.danchu.domain.store.exception.StoreErrorCode;
import com.likelion.danchu.global.exception.CustomException;

import reactor.core.publisher.Mono;

@Component
public class KakaoLocalClient {

  private final WebClient webClient;

  public KakaoLocalClient(
      WebClient.Builder builder,
      @Value("${kakao.local.base-url}") String baseUrl,
      @Value("${kakao.local.rest-api-key}") String apiKey) {
    this.webClient =
        builder
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + apiKey)
            .build();
  }

  // 주소 → 좌표 변환
  public Optional<Coords> geocode(String address) {
    if (address == null || address.trim().isEmpty()) {
      return Optional.empty();
    }

    KakaoLocalAddressResponse resp =
        webClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/v2/local/search/address.json")
                        .queryParam("query", address.trim())
                        .queryParam("analyze_type", "exact")
                        .queryParam("page", 1)
                        .queryParam("size", 1)
                        .build())
            .retrieve()
            // 메서드 레퍼런스 대신 람다 사용 (HttpStatusCode 인스턴스 메서드)
            .onStatus(
                status -> status.is4xxClientError(),
                r -> {
                  if (r.statusCode() == HttpStatus.FORBIDDEN) {
                    return Mono.error(new CustomException(StoreErrorCode.LOCAL_API_DISABLED));
                  }
                  return r.createException();
                })
            .onStatus(status -> status.is5xxServerError(), r -> r.createException())
            .bodyToMono(KakaoLocalAddressResponse.class)
            .block();

    if (resp == null || resp.documents() == null || resp.documents().isEmpty()) {
      return Optional.empty();
    }

    var doc = resp.documents().get(0); // x=경도(lon), y=위도(lat)
    return Optional.of(new Coords(Double.parseDouble(doc.x()), Double.parseDouble(doc.y())));
  }

  public record Coords(double lon, double lat) {}
}
