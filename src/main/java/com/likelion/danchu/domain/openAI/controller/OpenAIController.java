package com.likelion.danchu.domain.openAI.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.likelion.danchu.domain.openAI.service.OpenAIService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/openai")
@Tag(name = "OpenAI", description = "OpenAI 관련 API")
public class OpenAIController {

  private final OpenAIService openAIService;
}
