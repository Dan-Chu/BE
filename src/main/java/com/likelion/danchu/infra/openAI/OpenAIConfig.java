package com.likelion.danchu.infra.openAI;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenAIConfig {

  @Value("${openai.api.key}")
  private String apiKey;

  @Value("${openai.base-url}")
  private String baseUrl;

  @Value("${openai.embedding-model}")
  private String embeddingModel;

  @Value("${openai.timeout-millis}")
  private int timeoutMillis;

  @Bean
  public RestTemplate openAiRestTemplate() {
    var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(timeoutMillis);
    factory.setReadTimeout(timeoutMillis);

    RestTemplate rt = new RestTemplate(factory);
    rt.setInterceptors(
        List.of(
            (req, body, ex) -> {
              req.getHeaders().add("Authorization", "Bearer " + apiKey);
              req.getHeaders().add("Accept", "application/json");
              return ex.execute(req, body);
            }));
    return rt;
  }

  @Bean
  public OpenAIUtil openAiUtil(RestTemplate openAiRestTemplate) {
    return new OpenAIUtil(openAiRestTemplate, baseUrl, embeddingModel);
  }
}
