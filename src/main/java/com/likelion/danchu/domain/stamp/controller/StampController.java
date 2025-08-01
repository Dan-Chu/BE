package com.likelion.danchu.domain.stamp.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stamps")
@Tag(name = "Stamp", description = "Stamp 관련 API")
public class StampController {}
