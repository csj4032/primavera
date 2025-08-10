package com.genius.primavera.testcontainers.bean;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.LocalStackContainerSpec;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("LocalStack AWS 서비스 빈 통합 테스트")
class LocalStackBeanCreatorIntegrationTest {

    private LocalStackBeanCreator beanCreator;
    private LocalStackContainer container;
    private LocalStackContainerSpec spec;

    @BeforeAll
    void setUp() {
        beanCreator = new LocalStackBeanCreator();
        
        container = new LocalStackContainer(
                org.testcontainers.utility.DockerImageName.parse("localstack/localstack:2.3.0")
        ).withServices(
                LocalStackContainer.Service.S3,
                LocalStackContainer.Service.DYNAMODB,
                LocalStackContainer.Service.SQS,
                LocalStackContainer.Service.SNS
        );
        
        log.info("LocalStack 컨테이너를 시작합니다... (시간이 걸릴 수 있습니다)");
        container.start();
        
        spec = new LocalStackContainerSpec();
        spec.addServices(
                LocalStackContainerSpec.AwsService.S3,
                LocalStackContainerSpec.AwsService.DYNAMODB,
                LocalStackContainerSpec.AwsService.SQS,
                LocalStackContainerSpec.AwsService.SNS
        );
        spec.setDebugMode(false);
        spec.setEdgePort(4566);
        
        log.info("✅ LocalStack 컨테이너가 시작되었습니다. 엔드포인트: {}", container.getEndpoint());
        log.info("- 액세스 키: {}", container.getAccessKey());
        log.info("- 시크릿 키: {}", container.getSecretKey());
        log.info("- 리전: {}", container.getRegion());
    }

    @AfterAll
    void tearDown() {
        if (container != null && container.isRunning()) {
            container.stop();
            log.info("LocalStack 컨테이너가 중지되었습니다");
        }
    }

    @Test
    @Order(1)
    @DisplayName("실제 LocalStack 컨테이너에서 AWS 클라이언트 빈 생성")
    void testRealAwsClientCreation() {
        ContainerInfo containerInfo = new ContainerInfo(
                "integration-test-localstack",
                ContainerType.LOCALSTACK,
                container,
                spec
        );

        log.info("LocalStack 컨테이너에서 AWS 클라이언트들을 생성합니다...");
        
        Object result = assertDoesNotThrow(() -> beanCreator.createBean(containerInfo));
        assertNotNull(result, "생성된 결과가 null이면 안됩니다");
        
        if (result instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> clientMap = (Map<String, Object>) result;
            
            if (clientMap.isEmpty()) {
                log.info("⚠️ AWS SDK 의존성이 없어서 클라이언트가 생성되지 않았습니다");
            } else {
                log.info("✅ 생성된 AWS 클라이언트들: {}", clientMap.keySet());
                
                clientMap.forEach((beanName, client) -> {
                    assertNotNull(client, "클라이언트 " + beanName + "이 null이면 안됩니다");
                    log.info("  - {}: {}", beanName, client.getClass().getName());
                });
            }
            
        } else {
            log.info("✅ AWS 클라이언트 생성 결과: {} (Map이 아닌 형태)", result.getClass().getSimpleName());
        }
    }

    @Test
    @Order(2) 
    @DisplayName("개별 AWS 서비스별 클라이언트 생성 테스트")
    void testIndividualServiceClients() {
        LocalStackContainerSpec.AwsService[] testServices = {
                LocalStackContainerSpec.AwsService.S3,
                LocalStackContainerSpec.AwsService.DYNAMODB,
                LocalStackContainerSpec.AwsService.SQS,
                LocalStackContainerSpec.AwsService.SNS
        };

        for (LocalStackContainerSpec.AwsService service : testServices) {
            if (!beanCreator.isServiceSupported(service)) {
                log.info("⚠️ {} 서비스를 건너뜁니다 (AWS SDK 의존성 없음)", service);
                continue;
            }

            LocalStackContainerSpec singleServiceSpec = new LocalStackContainerSpec();
            singleServiceSpec.setServices(Set.of(service));
            
            ContainerInfo containerInfo = new ContainerInfo(
                    "test-" + service.name().toLowerCase(),
                    ContainerType.LOCALSTACK,
                    container,
                    singleServiceSpec
            );

            Object result = assertDoesNotThrow(() -> beanCreator.createBean(containerInfo),
                    service + " 서비스 클라이언트 생성 중 예외가 발생하면 안됩니다");
            
            assertNotNull(result, service + " 서비스 결과가 null이면 안됩니다");
            
            if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> clientMap = (Map<String, Object>) result;
                
                if (!clientMap.isEmpty()) {
                    assertEquals(1, clientMap.size(), service + " 서비스만 요청했으므로 1개 클라이언트만 생성되어야 합니다");
                    log.info("✅ {} 서비스 클라이언트 생성 성공: {}", service, clientMap.keySet().iterator().next());
                } else {
                    log.info("⚠️ {} 서비스 클라이언트가 생성되지 않았습니다 (AWS SDK 없음)", service);
                }
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("LocalStack 연결성 확인")
    void testLocalStackConnectivity() {
        assertTrue(container.isRunning(), "LocalStack 컨테이너가 실행 중이어야 합니다");
        
        String endpoint = container.getEndpoint().toString();
        assertNotNull(endpoint, "엔드포인트가 null이면 안됩니다");
        assertTrue(endpoint.startsWith("http"), "엔드포인트가 HTTP URL이어야 합니다");
        
        log.info("✅ LocalStack 연결성 확인 완료:");
        log.info("  - 컨테이너 실행 상태: {}", container.isRunning());
        log.info("  - 엔드포인트: {}", endpoint);
        log.info("  - 액세스 키: {}", container.getAccessKey());
        log.info("  - 리전: {}", container.getRegion());
    }

    @Test
    @Order(4)
    @DisplayName("AWS 서비스별 엔드포인트 확인")
    void testServiceEndpoints() {
        LocalStackContainer.Service[] services = {
                LocalStackContainer.Service.S3,
                LocalStackContainer.Service.DYNAMODB,
                LocalStackContainer.Service.SQS,
                LocalStackContainer.Service.SNS
        };

        for (LocalStackContainer.Service service : services) {
            String serviceEndpoint = container.getEndpointOverride(service).toString();
            assertNotNull(serviceEndpoint, service + " 엔드포인트가 null이면 안됩니다");
            assertTrue(serviceEndpoint.startsWith("http"), service + " 엔드포인트가 HTTP URL이어야 합니다");
            
            log.info("✅ {} 서비스 엔드포인트: {}", service, serviceEndpoint);
        }
    }

    @Test
    @Order(5)
    @DisplayName("다중 서비스 동시 생성 성능 테스트")
    void testMultipleServicePerformance() {
        LocalStackContainerSpec multiServiceSpec = new LocalStackContainerSpec();
        multiServiceSpec.addServices(
                LocalStackContainerSpec.AwsService.S3,
                LocalStackContainerSpec.AwsService.DYNAMODB,
                LocalStackContainerSpec.AwsService.SQS,
                LocalStackContainerSpec.AwsService.SNS
        );
        
        ContainerInfo containerInfo = new ContainerInfo(
                "multi-service-performance",
                ContainerType.LOCALSTACK,
                container,
                multiServiceSpec
        );

        long startTime = System.currentTimeMillis();
        
        Object result = assertDoesNotThrow(() -> beanCreator.createBean(containerInfo));
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertNotNull(result, "결과가 null이면 안됩니다");
        assertTrue(duration < 30000, "다중 서비스 생성이 30초 이내에 완료되어야 합니다"); // 30초 제한
        
        if (result instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> clientMap = (Map<String, Object>) result;
            log.info("✅ 다중 서비스 생성 성능: {}개 클라이언트를 {}ms에 생성", clientMap.size(), duration);
        } else {
            log.info("✅ 다중 서비스 생성 완료: {}ms", duration);
        }
    }
}