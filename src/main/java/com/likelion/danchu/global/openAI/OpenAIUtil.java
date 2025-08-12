package com.likelion.danchu.global.openAI;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.client.RestTemplate;

import com.likelion.danchu.domain.openAI.dto.request.OpenAIRequest;
import com.likelion.danchu.domain.openAI.dto.response.OpenAIResponse;

public class OpenAIUtil {

  private final RestTemplate restTemplate;
  private final String baseUrl;
  private final String embeddingModel;

  public OpenAIUtil(RestTemplate restTemplate, String baseUrl, String embeddingModel) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
    this.embeddingModel = embeddingModel;
  }

  /** 여러 문장을 한 번에 임베딩 */
  public List<double[]> embedAll(List<String> inputs) {
    if (inputs == null || inputs.isEmpty()) return List.of();

    OpenAIRequest req = new OpenAIRequest(embeddingModel, inputs);
    OpenAIResponse res =
        restTemplate.postForObject(baseUrl + "/embeddings", req, OpenAIResponse.class);

    if (res == null || res.getData() == null || res.getData().isEmpty()) return List.of();

    List<double[]> out = new ArrayList<>(res.getData().size());
    for (var d : res.getData()) {
      List<Double> v = d.getEmbedding();
      double[] arr = new double[v.size()];
      for (int i = 0; i < v.size(); i++) arr[i] = v.get(i);
      out.add(arr);
    }
    return out;
  }

  /** 코사인 유사도 */
  public static double cosine(double[] a, double[] b) {
    if (a == null || b == null || a.length != b.length) return -1.0;
    double dot = 0.0, na = 0.0, nb = 0.0;
    for (int i = 0; i < a.length; i++) {
      dot += a[i] * b[i];
      na += a[i] * a[i];
      nb += b[i] * b[i];
    }
    if (na == 0 || nb == 0) return -1.0;
    return dot / (Math.sqrt(na) * Math.sqrt(nb));
  }
}
