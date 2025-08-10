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
        log.debug("LocalStack AWS service translated_text_5 translated_text_5 translated_text_6...");

        registerFactory(new S3ClientFactory());
        registerFactory(new DynamoDbClientFactory());
        registerFactory(new SqsClientFactory());
        registerFactory(new SnsClientFactory());
        registerFactory(new LambdaClientFactory());

        log.info(" {}translated_text_2 AWS service translated_text_5 translated_text_4 translated_text_17", clientFactories.size());
    }

    private void registerFactory(AwsServiceClientFactory factory) {
        if (factory.isAvailable()) {
            clientFactories.put(factory.getSupportedService(), factory);
            log.debug(" {} translated_text_4 translated_text_17 (dependency translated_text_2 translated_text_2)", factory.getSupportedService());
        } else {
            log.debug(" {} translated_text_4 translated_text_5 (dependency translated_text_2)", factory.getSupportedService());
        }
    }

    @Override
    public Object createBean(ContainerInfo containerInfo) {
        if (!(containerInfo.container() instanceof LocalStackContainer)) {
            throw new IllegalArgumentException("LocalStackContainertranslated_text_1 translated_text_5: " + containerInfo.container().getClass());
        }

        if (!(containerInfo.spec() instanceof LocalStackContainerSpec)) {
            throw new IllegalArgumentException("LocalStackContainerSpectranslated_text_1 translated_text_5: " + containerInfo.spec().getClass());
        }

        LocalStackContainer container = (LocalStackContainer) containerInfo.container();
        LocalStackContainerSpec spec = (LocalStackContainerSpec) containerInfo.spec();

        log.info("LocalStack translated_text_1 '{}' translated_text_2 translated_text_4 AWS servicetranslated_text_1 translated_text_2 translated_text_5 translated_text_3 translated_text_11", 
                containerInfo.name());

        Map<String, Object> awsClients = new LinkedHashMap<>();
        Set<LocalStackContainerSpec.AwsService> activeServices = spec.getServices();

        if (activeServices == null || activeServices.isEmpty()) {
            log.warn("translated_text_4 AWS servicetranslated_text_1 translated_text_4. translated_text_2 servicetranslated_text_1 translated_text_2.");
            activeServices = getDefaultServices();
        }

        log.debug("translated_text_4 AWS service: {}", activeServices);

        for (LocalStackContainerSpec.AwsService service : activeServices) {
            try {
                createClientForService(service, container, awsClients);
            } catch (Exception e) {
                log.warn("AWS service {} translated_text_5 creation failure: {}", service, e.getMessage(), e);
            }
        }

        if (awsClients.isEmpty()) {
            log.warn("creation AWS translated_text_5translated_text_1 translated_text_4. AWS SDK dependencytranslated_text_1 translated_text_16.");
            return Collections.emptyMap();
        }

        log.info(" {}translated_text_2 AWS translated_text_5translated_text_1 translated_text_10 creation: {}", 
                awsClients.size(), awsClients.keySet());
        
        return awsClients;
    }

    private void createClientForService(LocalStackContainerSpec.AwsService service, 
                                      LocalStackContainer container, 
                                      Map<String, Object> awsClients) {
        
        AwsServiceClientFactory factory = clientFactories.get(service);
        
        if (factory == null) {
            log.debug("service {}translated_text_1 translated_text_2 translated_text_4 translated_text_1 translated_text_1 translated_text_4. translated_text_4 translated_text_2 servicetranslated_text_1 dependencytranslated_text_1 translated_text_4.", service);
            return;
        }

        if (!factory.isAvailable()) {
            log.debug("service {} translated_text_4 dependencytranslated_text_1 translated_text_2 translated_text_1 translated_text_4.", service);
            return;
        }

        try {
            Object client = factory.createClient(container);
            String beanName = factory.getBeanName();
            
            awsClients.put(beanName, client);
            
            log.debug(" {}({}) translated_text_5translated_text_1 creation '{}' translated_text_1 translated_text_17", 
                    service, client.getClass().getSimpleName(), beanName);
                    
        } catch (Exception e) {
            log.error("service {} translated_text_5 creation translated_text_1 exception translated_text_2", service, e);
            throw new RuntimeException("AWS " + service + " translated_text_5 creation failure", e);
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