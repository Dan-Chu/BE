package com.likelion.danchu.domain.hashtag.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hashtags")
@Tag(name = "Hashtag", description = "HashTag 관련 API")
public class HashtagController {}
