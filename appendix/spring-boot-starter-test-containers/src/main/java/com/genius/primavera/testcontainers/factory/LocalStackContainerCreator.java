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

public class LocalStackContainerCreator implements ContainerCreator {
    
    @Override
    public GenericContainer<?> create(BaseContainerSpec spec) {
        String image = spec.getImage() != null ? spec.getImage() : ContainerType.LOCALSTACK.getDefaultImage();
        Integer timeout = spec.getStartupTimeout() != null ? spec.getStartupTimeout() : 60;
        
        LocalStackContainer container = new LocalStackContainer(DockerImageName.parse(image))
                .withStartupTimeout(Duration.ofSeconds(timeout));
        
        if (spec instanceof LocalStackContainerSpec localStackSpec) {
            if (localStackSpec.getServices() != null && !localStackSpec.getServices().isEmpty()) {
                Set<LocalStackContainer.Service> services = localStackSpec.getServices().stream()
                        .map(this::mapToLocalStackService)
                        .collect(Collectors.toSet());
                container.withServices(services.toArray(new LocalStackContainer.Service[0]));
            } else {
                container.withServices(
                    LocalStackContainer.Service.S3,
                    LocalStackContainer.Service.DYNAMODB,
                    LocalStackContainer.Service.SQS,
                    LocalStackContainer.Service.SNS
                );
            }
            
            if (localStackSpec.getDebugMode() != null && localStackSpec.getDebugMode()) {
                container.withEnv("DEBUG", "1");
            }
            
            if (localStackSpec.getLambdaExecutor() != null) {
                String executorValue = switch (localStackSpec.getLambdaExecutor()) {
                    case DOCKER -> "docker";
                    case LOCAL -> "local";
                };
                container.withEnv("LAMBDA_EXECUTOR", executorValue);
            }
            
            if (localStackSpec.getExternalHostName() != null) {
                container.withEnv("HOSTNAME_EXTERNAL", localStackSpec.getExternalHostName());
            }
            
            if (localStackSpec.getEdgePort() != null) {
                container.withEnv("EDGE_PORT", localStackSpec.getEdgePort().toString());
            }
            
            if (localStackSpec.getUseLegacyPorts() != null) {
                container.withEnv("USE_LEGACY_PORTS", localStackSpec.getUseLegacyPorts().toString());
            }
            
            if (localStackSpec.getDataDirectory() != null) {
                container.withEnv("DATA_DIR", localStackSpec.getDataDirectory());
            }
            
            if (localStackSpec.getDockerNetworkMode() != null) {
                container.withEnv("DOCKER_HOST", localStackSpec.getDockerNetworkMode());
            }
        } else {
            container.withServices(
                LocalStackContainer.Service.S3,
                LocalStackContainer.Service.DYNAMODB,
                LocalStackContainer.Service.SQS,
                LocalStackContainer.Service.SNS
            );
        }
        
        if (spec.getEnvironment() != null) {
            spec.getEnvironment().forEach(container::withEnv);
        }
        
        return container;
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.LOCALSTACK;
    }
    
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