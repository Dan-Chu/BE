package com.likelion.danchu.global.config;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import lombok.Getter;

@Getter
@Configuration
public class S3Config {

  private AWSCredentials awsCredentials;

  @Value("${cloud.aws.credentials.access-key}")
  private String accessKey;

  @Value("${cloud.aws.credentials.secret-key}")
  private String secretKey;

  @Value("${cloud.aws.region.static}")
  private String region;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${cloud.aws.s3.path.user}")
  private String userFolder;

  @Value("${cloud.aws.s3.path.store}")
  private String storeFolder;

  @Value("${cloud.aws.s3.path.reward}")
  private String rewardFolder;

  @Value("${cloud.aws.s3.path.menu}")
  private String menuFolder;

  @PostConstruct
  public void init() {
    this.awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
  }

  @Bean
  public AmazonS3 amazonS3() {
    return AmazonS3ClientBuilder.standard()
        .withRegion(region)
        .withCredentials(awsCredentialsProvider())
        .build();
  }

  @Bean
  public AWSCredentialsProvider awsCredentialsProvider() {
    return new AWSStaticCredentialsProvider(awsCredentials);
  }
}
