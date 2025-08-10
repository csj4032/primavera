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
        log.debug("LocalStack AWS service Endpoint file exists...");

        registerFactory(new S3ClientFactory());
        registerFactory(new DynamoDbClientFactory());
        registerFactory(new SqsClientFactory());
        registerFactory(new SnsClientFactory());
        registerFactory(new LambdaClientFactory());

        log.info(" {}test AWS service Endpoint file should7", clientFactories.size());
    }

    private void registerFactory(AwsServiceClientFactory factory) {
        if (factory.isAvailable()) {
            clientFactories.put(factory.getSupportedService(), factory);
            log.debug(" {} file should7 (dependency test)", factory.getSupportedService());
        } else {
            log.debug(" {} file Endpoint (dependency test)", factory.getSupportedService());
        }
    }

    @Override
    public Object createBean(ContainerInfo containerInfo) {
        if (!(containerInfo.container() instanceof LocalStackContainer)) {
            throw new IllegalArgumentException("LocalStackContainershould Endpoint: " + containerInfo.container().getClass());
        }

        if (!(containerInfo.spec() instanceof LocalStackContainerSpec)) {
            throw new IllegalArgumentException("LocalStackContainerSpecshould Endpoint: " + containerInfo.spec().getClass());
        }

        LocalStackContainer container = (LocalStackContainer) containerInfo.container();
        LocalStackContainerSpec spec = (LocalStackContainerSpec) containerInfo.spec();

        log.info("LocalStack should '{}' test file AWS serviceshould test Endpoint connection processing", 
                containerInfo.name());

        Map<String, Object> awsClients = new LinkedHashMap<>();
        Set<LocalStackContainerSpec.AwsService> activeServices = spec.getServices();

        if (activeServices == null || activeServices.isEmpty()) {
            log.warn("file AWS serviceshould file. test serviceshould test.");
            activeServices = getDefaultServices();
        }

        log.debug("file AWS service: {}", activeServices);

        for (LocalStackContainerSpec.AwsService service : activeServices) {
            try {
                createClientForService(service, container, awsClients);
            } catch (Exception e) {
                log.warn("AWS service {} Endpoint creation failure: {}", service, e.getMessage(), e);
            }
        }

        if (awsClients.isEmpty()) {
            log.warn("creation AWS Endpointshould file. AWS SDK  dependency needs to be added6.");
            return Collections.emptyMap();
        }

        log.info(" {}test AWS Endpointneeds to be added0 creation: {}", 
                awsClients.size(), awsClients.keySet());
        
        return awsClients;
    }

    private void createClientForService(LocalStackContainerSpec.AwsService service, 
                                      LocalStackContainer container, 
                                      Map<String, Object> awsClients) {
        
        AwsServiceClientFactory factory = clientFactories.get(service);
        
        if (factory == null) {
            log.debug("service {}should test file needs to be added file. file test serviceshould dependencyshould file.", service);
            return;
        }

        if (!factory.isAvailable()) {
            log.debug("service {} file dependencytest should file.", service);
            return;
        }

        try {
            Object client = factory.createClient(container);
            String beanName = factory.getBeanName();
            
            awsClients.put(beanName, client);
            
            log.debug(" {}({}) Endpointshould creation '{}' needs to be added7", 
                    service, client.getClass().getSimpleName(), beanName);
                    
        } catch (Exception e) {
            log.error("service {} Endpoint creation should exception test", service, e);
            throw new RuntimeException("AWS " + service + " Endpoint creation failure", e);
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