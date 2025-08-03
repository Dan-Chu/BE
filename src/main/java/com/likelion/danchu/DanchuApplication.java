package com.likelion.danchu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DanchuApplication {

  public static void main(String[] args) {
    SpringApplication.run(DanchuApplication.class, args);
  }
}
