package com.likelion.danchu.domain.openAI.dto.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.likelion.danchu.domain.openAI.dto.response.Message;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAIRequest {
  private String model;
  private List<Message> messages = new ArrayList<>();
  private Double temperature;

  @JsonProperty("max_tokens")
  private Integer maxTokens;

  @JsonProperty("response_format")
  private Map<String, Object> responseFormat;

  public OpenAIRequest(String model, String prompt, Double temperature, Integer maxTokens) {
    this.model = model;
    this.temperature = temperature;
    this.maxTokens = maxTokens;
    this.messages.add(new Message("user", prompt));
    this.responseFormat = Map.of("type", "json_object");
  }
}
