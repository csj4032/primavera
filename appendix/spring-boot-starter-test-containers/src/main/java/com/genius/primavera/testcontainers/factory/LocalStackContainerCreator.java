package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.LocalStackContainerSpec;
import com.genius.primavera.testcontainers.ContainerCreator;
import com.genius.primavera.testcontainers.ContainerType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * LocalStack 컨테이너 생성 팩토리
 * AWS 서비스들을 로컬에서 모킹하는 LocalStack 컨테이너를 생성합니다.
 */
public class LocalStackContainerCreator implements ContainerCreator {
    
    @Override
    public GenericContainer<?> create(BaseContainerSpec spec) {
        String image = spec.getImage() != null ? spec.getImage() : ContainerType.LOCALSTACK.getDefaultImage();
        Integer timeout = spec.getStartupTimeout() != null ? spec.getStartupTimeout() : 60;
        
        LocalStackContainer container = new LocalStackContainer(DockerImageName.parse(image))
                .withStartupTimeout(Duration.ofSeconds(timeout));
        
        if (spec instanceof LocalStackContainerSpec localStackSpec) {
            // AWS 서비스 설정
            if (localStackSpec.getServices() != null && !localStackSpec.getServices().isEmpty()) {
                Set<LocalStackContainer.Service> services = localStackSpec.getServices().stream()
                        .map(this::mapToLocalStackService)
                        .collect(Collectors.toSet());
                container.withServices(services.toArray(new LocalStackContainer.Service[0]));
            } else {
                // 기본 서비스 설정
                container.withServices(
                    LocalStackContainer.Service.S3,
                    LocalStackContainer.Service.DYNAMODB,
                    LocalStackContainer.Service.SQS,
                    LocalStackContainer.Service.SNS
                );
            }
            
            // 디버그 모드 설정
            if (localStackSpec.getDebugMode() != null && localStackSpec.getDebugMode()) {
                container.withEnv("DEBUG", "1");
            }
            
            // Lambda 실행기 설정
            if (localStackSpec.getLambdaExecutor() != null) {
                String executorValue = switch (localStackSpec.getLambdaExecutor()) {
                    case DOCKER -> "docker";
                    case LOCAL -> "local";
                    case DOCKER_REUSE -> "docker-reuse";
                };
                container.withEnv("LAMBDA_EXECUTOR", executorValue);
            }
            
            // 외부 호스트명 설정
            if (localStackSpec.getExternalHostName() != null) {
                container.withEnv("HOSTNAME_EXTERNAL", localStackSpec.getExternalHostName());
            }
            
            // 에지 포트 설정
            if (localStackSpec.getEdgePort() != null) {
                container.withEnv("EDGE_PORT", localStackSpec.getEdgePort().toString());
            }
            
            // 레가시 포트 사용 설정
            if (localStackSpec.getUseLegacyPorts() != null) {
                container.withEnv("USE_LEGACY_PORTS", localStackSpec.getUseLegacyPorts().toString());
            }
            
            // 데이터 디렉토리 설정
            if (localStackSpec.getDataDirectory() != null) {
                container.withEnv("DATA_DIR", localStackSpec.getDataDirectory());
            }
            
            // Docker 네트워크 모드 설정
            if (localStackSpec.getDockerNetworkMode() != null) {
                container.withEnv("DOCKER_HOST", localStackSpec.getDockerNetworkMode());
            }
        } else {
            // 기본 설정 적용
            container.withServices(
                LocalStackContainer.Service.S3,
                LocalStackContainer.Service.DYNAMODB,
                LocalStackContainer.Service.SQS,
                LocalStackContainer.Service.SNS
            );
        }
        
        // 공통 환경 변수 적용
        if (spec.getEnvironment() != null) {
            spec.getEnvironment().forEach(container::withEnv);
        }
        
        return container;
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.LOCALSTACK;
    }
    
    /**
     * LocalStackContainerSpec.AwsService를 LocalStackContainer.Service로 매핑
     */
    private LocalStackContainer.Service mapToLocalStackService(LocalStackContainerSpec.AwsService service) {
        return switch (service) {
            case S3 -> LocalStackContainer.Service.S3;
            case DYNAMODB -> LocalStackContainer.Service.DYNAMODB;
            case SQS -> LocalStackContainer.Service.SQS;
            case SNS -> LocalStackContainer.Service.SNS;
            case LAMBDA -> LocalStackContainer.Service.LAMBDA;
            case APIGATEWAY -> LocalStackContainer.Service.API_GATEWAY;
            case CLOUDFORMATION -> LocalStackContainer.Service.CLOUDFORMATION;
            case SES -> LocalStackContainer.Service.SES;
            case KINESIS -> LocalStackContainer.Service.KINESIS;
            case SECRETSMANAGER -> LocalStackContainer.Service.SECRETSMANAGER;
            case CLOUDWATCH -> LocalStackContainer.Service.CLOUDWATCH;
            case IAM -> LocalStackContainer.Service.IAM;
            case EC2 -> LocalStackContainer.Service.EC2;
            case STEPFUNCTIONS -> LocalStackContainer.Service.STEPFUNCTIONS;
            case SSM -> LocalStackContainer.Service.SSM;
            default -> throw new IllegalArgumentException("Unsupported LocalStack service: " + service);
        };
    }
}