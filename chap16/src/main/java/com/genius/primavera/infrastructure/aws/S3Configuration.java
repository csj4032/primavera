package com.genius.primavera.infrastructure.aws;

import io.awspring.cloud.s3.S3Template;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Configuration {
    
    // Spring Cloud AWS가 자동으로 S3Template과 S3Client를 설정합니다.
    // application.yml의 spring.cloud.aws.* 설정을 사용합니다.
}