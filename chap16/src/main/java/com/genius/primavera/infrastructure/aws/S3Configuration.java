package com.genius.primavera.infrastructure.aws;

import io.awspring.cloud.s3.S3Template;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Configuration {
}