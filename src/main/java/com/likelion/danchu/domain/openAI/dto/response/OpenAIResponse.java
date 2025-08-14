package com.likelion.danchu.domain.openAI.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIResponse {
  private List<Datum> data;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Datum {
    private List<Double> embedding;
    private int index;
    private String object;
  }
}
