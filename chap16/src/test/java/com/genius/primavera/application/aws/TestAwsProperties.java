package com.genius.primavera.application.aws;

import org.springframework.boot.context.properties.bind.Name;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 테스트용 AWS 설정 프로퍼티
 * application-test.yml의 spring.cloud.aws 설정을 바인딩합니다.
 */
@ConfigurationProperties(prefix = "spring.cloud.aws")
public record TestAwsProperties(
    Credentials credentials,
    Region region,
    S3 s3
) {
    
    public record Credentials(
        String accessKey,
        String secretKey
    ) {}
    
    public record Region(
        @Name("static") String value
    ) {
        // region.static을 region.value로 매핑 (@Name 어노테이션 사용)
    }
    
    public record S3(
        String bucketName,
        String endpoint
    ) {
        public boolean hasEndpoint() {
            return endpoint != null && !endpoint.isEmpty();
        }
        
        public boolean isLocalStack() {
            return hasEndpoint() && endpoint.contains("localstack");
        }
    }
    
    /**
     * LocalStack 사용 여부 확인
     */
    public boolean isLocalStack() {
        return "test".equals(credentials.accessKey()) || s3.isLocalStack();
    }
    
    /**
     * 실제 AWS 환경 사용 여부 확인
     */
    public boolean isRealAws() {
        return !isLocalStack();
    }
}