package com.likelion.danchu.global.openAI;

import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.danchu.domain.openAI.dto.request.OpenAIRequest;

public class OpenAIUtil {

  private final RestTemplate restTemplate;
  private final String baseUrl;
  private final String model;
  private final Double temperature;
  private final Integer maxTokens;
  private static final ObjectMapper OM = new ObjectMapper();

  public OpenAIUtil(
      RestTemplate restTemplate,
      String baseUrl,
      String model,
      Double temperature,
      Integer maxTokens) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
    this.model = model;
    this.temperature = temperature;
    this.maxTokens = maxTokens;
  }

  public String chat(String prompt) {
    OpenAIRequest req = new OpenAIRequest(model, prompt, temperature, maxTokens);

    String raw = restTemplate.postForObject(baseUrl + "/chat/completions", req, String.class);
    if (raw == null || raw.isBlank()) return "";

    try {
      JsonNode root = OM.readTree(raw);
      JsonNode content = root.path("choices").path(0).path("message").path("content");
      if (content.isTextual()) return content.asText();

      // 혹시 배열 파츠 형태일 때 text 필드 이어붙이기
      if (content.isArray()) {
        StringBuilder sb = new StringBuilder();
        for (JsonNode p : content) {
          String t = p.path("text").asText("");
          if (!t.isBlank()) sb.append(t);
        }
        return sb.toString();
      }
      return "";
    } catch (Exception ignore) {
      return "";
    }
  }
}
