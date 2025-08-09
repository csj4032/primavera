package com.genius.primavera.testcontainers.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;

/**
 * LocalStack 컨테이너 전용 설정
 * BaseContainerSpec의 공통 설정에 LocalStack 고유 설정을 추가합니다.
 * LocalStack은 AWS 서비스들을 로컬에서 모킹하는 도구입니다.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties
@Validated
public class LocalStackContainerSpec extends BaseContainerSpec {

    /**
     * 활성화할 AWS 서비스 목록
     * 기본값: [S3, DYNAMODB, SQS, SNS]
     * 지원 서비스: S3, DYNAMODB, SQS, SNS, LAMBDA, APIGATEWAY, CLOUDFORMATION, SES, KINESIS, SECRETSMANAGER 등
     */
    @NotNull(message = "Services cannot be null")
    private Set<AwsService> services = Set.of(AwsService.S3, AwsService.DYNAMODB, AwsService.SQS, AwsService.SNS);

    /**
     * 디버그 모드 활성화
     * 기본값: false
     * true로 설정하면 상세한 로그가 출력됩니다.
     */
    private Boolean debugMode = false;

    /**
     * 데이터 디렉토리 경로
     * 기본값: null (임시 디렉토리 사용)
     * 설정 시 데이터가 호스트 디렉토리에 저장됩니다.
     */
    private String dataDirectory;

    /**
     * Lambda 실행기 설정
     * 기본값: DOCKER
     * Lambda 함수 실행 환경을 설정합니다.
     */
    private LambdaExecutor lambdaExecutor = LambdaExecutor.DOCKER;

    /**
     * Docker 네트워크 모드
     * 기본값: null (기본 네트워크 사용)
     */
    private String dockerNetworkMode;

    /**
     * 외부 호스트명
     * 기본값: null (자동 감지)
     * LocalStack 서비스가 외부에서 접근할 때 사용할 호스트명
     */
    private String externalHostName;

    /**
     * 에지 포트 설정
     * 기본값: 4566
     * 모든 서비스가 사용할 통합 포트
     */
    @Min(value = 1024, message = "Edge port must be at least 1024")
    @Max(value = 65535, message = "Edge port must not exceed 65535")
    private Integer edgePort = 4566;

    /**
     * 레가시 포트 사용 여부
     * 기본값: false
     * true로 설정하면 각 서비스가 개별 포트를 사용합니다.
     */
    private Boolean useLegacyPorts = false;

    /**
     * 지원되는 AWS 서비스 목록
     */
    public enum AwsService {
        /** Amazon S3 - Simple Storage Service */
        S3,
        /** Amazon DynamoDB - NoSQL Database */
        DYNAMODB,
        /** Amazon SQS - Simple Queue Service */
        SQS,
        /** Amazon SNS - Simple Notification Service */
        SNS,
        /** AWS Lambda - Serverless Computing */
        LAMBDA,
        /** Amazon API Gateway - API Management */
        APIGATEWAY,
        /** AWS CloudFormation - Infrastructure as Code */
        CLOUDFORMATION,
        /** Amazon SES - Simple Email Service */
        SES,
        /** Amazon Kinesis - Real-time Data Streaming */
        KINESIS,
        /** AWS Secrets Manager - Secret Management */
        SECRETSMANAGER,
        /** Amazon CloudWatch - Monitoring */
        CLOUDWATCH,
        /** AWS IAM - Identity and Access Management */
        IAM,
        /** Amazon EC2 - Elastic Compute Cloud */
        EC2,
        /** AWS Step Functions - Workflow Service */
        STEPFUNCTIONS,
        /** AWS Systems Manager - System Management */
        SSM
    }

    /**
     * Lambda 실행기 옵션
     */
    public enum LambdaExecutor {
        /** Docker 기반 실행 (기본값) */
        DOCKER,
        /** 로컬 실행 */
        LOCAL,
        /** Docker 재사용 */
        DOCKER_REUSE
    }

    /**
     * 서비스 추가
     * 
     * @param service 추가할 AWS 서비스
     */
    public void addService(AwsService service) {
        if (this.services == null) {
            this.services = new HashSet<>();
        }
        this.services.add(service);
    }

    /**
     * 여러 서비스 추가
     * 
     * @param services 추가할 AWS 서비스들
     */
    public void addServices(AwsService... services) {
        if (this.services == null) {
            this.services = new HashSet<>();
        }
        for (AwsService service : services) {
            this.services.add(service);
        }
    }

    /**
     * 서비스 제거
     * 
     * @param service 제거할 AWS 서비스
     */
    public void removeService(AwsService service) {
        if (this.services != null) {
            this.services.remove(service);
        }
    }

    /**
     * 특정 서비스 활성화 여부 확인
     * 
     * @param service 확인할 AWS 서비스
     * @return 활성화 여부
     */
    public boolean hasService(AwsService service) {
        return this.services != null && this.services.contains(service);
    }

    /**
     * S3 서비스 활성화 여부
     * 
     * @return S3 활성화 여부
     */
    public boolean isS3Enabled() {
        return hasService(AwsService.S3);
    }

    /**
     * DynamoDB 서비스 활성화 여부
     * 
     * @return DynamoDB 활성화 여부
     */
    public boolean isDynamoDbEnabled() {
        return hasService(AwsService.DYNAMODB);
    }

    /**
     * SQS 서비스 활성화 여부
     * 
     * @return SQS 활성화 여부
     */
    public boolean isSqsEnabled() {
        return hasService(AwsService.SQS);
    }

    /**
     * SNS 서비스 활성화 여부
     * 
     * @return SNS 활성화 여부
     */
    public boolean isSnsEnabled() {
        return hasService(AwsService.SNS);
    }

    /**
     * Lambda 서비스 활성화 여부
     * 
     * @return Lambda 활성화 여부
     */
    public boolean isLambdaEnabled() {
        return hasService(AwsService.LAMBDA);
    }
}