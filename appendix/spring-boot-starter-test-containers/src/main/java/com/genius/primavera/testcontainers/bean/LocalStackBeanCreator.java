package com.genius.primavera.testcontainers.bean;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.bean.aws.*;
import com.genius.primavera.testcontainers.config.LocalStackContainerSpec;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LocalStackBeanCreator implements BeanCreator {

    private final Map<LocalStackContainerSpec.AwsService, AwsServiceClientFactory> clientFactories;

    public LocalStackBeanCreator() {
        this.clientFactories = new ConcurrentHashMap<>();
        initializeFactories();
    }

    private void initializeFactories() {
        log.debug("LocalStack AWS 서비스 클라이언트 팩토리들을 초기화합니다...");

        registerFactory(new S3ClientFactory());
        registerFactory(new DynamoDbClientFactory());
        registerFactory(new SqsClientFactory());
        registerFactory(new SnsClientFactory());
        registerFactory(new LambdaClientFactory());


        log.info("✅ {}개의 AWS 서비스 클라이언트 팩토리가 등록되었습니다", clientFactories.size());
    }

    private void registerFactory(AwsServiceClientFactory factory) {
        if (factory.isAvailable()) {
            clientFactories.put(factory.getSupportedService(), factory);
            log.debug("✅ {} 팩토리가 등록되었습니다 (의존성 사용 가능)", factory.getSupportedService());
        } else {
            log.debug("⚠️ {} 팩토리를 건너뜁니다 (의존성 없음)", factory.getSupportedService());
        }
    }

    @Override
    public Object createBean(ContainerInfo containerInfo) {
        if (!(containerInfo.container() instanceof LocalStackContainer)) {
            throw new IllegalArgumentException("LocalStackContainer가 필요합니다: " + containerInfo.container().getClass());
        }

        if (!(containerInfo.spec() instanceof LocalStackContainerSpec)) {
            throw new IllegalArgumentException("LocalStackContainerSpec이 필요합니다: " + containerInfo.spec().getClass());
        }

        LocalStackContainer container = (LocalStackContainer) containerInfo.container();
        LocalStackContainerSpec spec = (LocalStackContainerSpec) containerInfo.spec();

        log.info("LocalStack 컨테이너 '{}' 에서 활성화된 AWS 서비스들을 위한 클라이언트 빈들을 생성합니다", 
                containerInfo.name());

        Map<String, Object> awsClients = new LinkedHashMap<>();
        Set<LocalStackContainerSpec.AwsService> activeServices = spec.getServices();

        if (activeServices == null || activeServices.isEmpty()) {
            log.warn("활성화된 AWS 서비스가 없습니다. 기본 서비스들을 사용합니다.");
            activeServices = getDefaultServices();
        }

        log.debug("활성화된 AWS 서비스들: {}", activeServices);

        for (LocalStackContainerSpec.AwsService service : activeServices) {
            try {
                createClientForService(service, container, awsClients);
            } catch (Exception e) {
                log.warn("AWS 서비스 {} 클라이언트 생성 실패: {}", service, e.getMessage(), e);
            }
        }

        if (awsClients.isEmpty()) {
            log.warn("생성된 AWS 클라이언트가 없습니다. AWS SDK 의존성을 확인해주세요.");
            return Collections.emptyMap();
        }

        log.info("✅ {}개의 AWS 클라이언트가 성공적으로 생성되었습니다: {}", 
                awsClients.size(), awsClients.keySet());
        
        return awsClients;
    }

    private void createClientForService(LocalStackContainerSpec.AwsService service, 
                                      LocalStackContainer container, 
                                      Map<String, Object> awsClients) {
        
        AwsServiceClientFactory factory = clientFactories.get(service);
        
        if (factory == null) {
            log.debug("서비스 {}에 대한 팩토리를 찾을 수 없습니다. 지원되지 않는 서비스이거나 의존성이 없습니다.", service);
            return;
        }

        if (!factory.isAvailable()) {
            log.debug("서비스 {} 팩토리의 의존성이 사용할 수 없습니다.", service);
            return;
        }

        try {
            Object client = factory.createClient(container);
            String beanName = factory.getBeanName();
            
            awsClients.put(beanName, client);
            
            log.debug("✅ {}({}) 클라이언트가 생성되어 '{}' 이름으로 등록되었습니다", 
                    service, client.getClass().getSimpleName(), beanName);
                    
        } catch (Exception e) {
            log.error("서비스 {} 클라이언트 생성 중 예외 발생", service, e);
            throw new RuntimeException("AWS " + service + " 클라이언트 생성 실패", e);
        }
    }

    private Set<LocalStackContainerSpec.AwsService> getDefaultServices() {
        return Set.of(
                LocalStackContainerSpec.AwsService.S3,
                LocalStackContainerSpec.AwsService.DYNAMODB,
                LocalStackContainerSpec.AwsService.SQS,
                LocalStackContainerSpec.AwsService.SNS
        );
    }

    @Override
    public ContainerType getSupportedType() {
        return ContainerType.LOCALSTACK;
    }

    public Set<LocalStackContainerSpec.AwsService> getSupportedServices() {
        return new HashSet<>(clientFactories.keySet());
    }

    public boolean isServiceSupported(LocalStackContainerSpec.AwsService service) {
        AwsServiceClientFactory factory = clientFactories.get(service);
        return factory != null && factory.isAvailable();
    }

    public Optional<AwsServiceClientFactory> getFactory(LocalStackContainerSpec.AwsService service) {
        return Optional.ofNullable(clientFactories.get(service));
    }
}