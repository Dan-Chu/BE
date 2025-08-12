package com.likelion.danchu.domain.openAI.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAIRequest {
  private String model;

  @JsonProperty("input")
  private List<String> inputs;

  public OpenAIRequest(String model, List<String> inputs) {
    this.model = model;
    this.inputs = inputs;
  }
}
