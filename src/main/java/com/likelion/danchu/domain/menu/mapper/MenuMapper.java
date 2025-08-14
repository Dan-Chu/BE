package com.likelion.danchu.domain.menu.mapper;

import org.springframework.stereotype.Component;

import com.likelion.danchu.domain.menu.dto.request.MenuRequest;
import com.likelion.danchu.domain.menu.dto.response.MenuResponse;
import com.likelion.danchu.domain.menu.entity.Menu;
import com.likelion.danchu.domain.store.entity.Store;

@Component
public class MenuMapper {

  // MenuRequest + 업로드된 이미지 URL로 Menu 생성
  public Menu toEntity(MenuRequest menuRequest, Store store, String uploadedUrl) {
    String finalUrl = (uploadedUrl != null && !uploadedUrl.isBlank()) ? uploadedUrl : null;

    return Menu.builder()
        .name(menuRequest.getName())
        .price(menuRequest.getPrice())
        .imageUrl(finalUrl) // 업로드 파일이 없으면 null
        .store(store)
        .build();
  }

  public MenuResponse toResponse(Menu menu) {
    Integer price = menu.getPrice();

    return MenuResponse.builder()
        .id(menu.getId())
        .storeId(menu.getStore().getId())
        .storeName(menu.getStore().getName())
        .name(menu.getName())
        .price(price) // 숫자 그대로 9000
        .priceFormatted(formatWon(price)) // 9,000원
        .imageUrl(menu.getImageUrl())
        .build();
  }

  private String formatWon(Integer price) {
    if (price == null) {
      return null;
    }
    return String.format("%,d원", price);
  }
}
