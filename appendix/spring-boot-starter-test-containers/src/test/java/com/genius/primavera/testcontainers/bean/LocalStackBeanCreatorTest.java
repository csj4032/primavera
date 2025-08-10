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
@DisplayName("LocalStack AWS 서비스 빈 생성 테스트")
class LocalStackBeanCreatorTest {

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
        
        spec = new LocalStackContainerSpec();
        spec.addServices(
                LocalStackContainerSpec.AwsService.S3,
                LocalStackContainerSpec.AwsService.DYNAMODB,
                LocalStackContainerSpec.AwsService.SQS,
                LocalStackContainerSpec.AwsService.SNS
        );
        spec.setDebugMode(false);
        spec.setEdgePort(4566);
        
        log.info("LocalStackBeanCreator 테스트 환경이 설정되었습니다");
    }

    @Test
    @Order(1)
    @DisplayName("LocalStackBeanCreator 지원 타입 확인")
    void testSupportedType() {
        assertEquals(ContainerType.LOCALSTACK, beanCreator.getSupportedType());
        log.info("✅ LocalStackBeanCreator 지원 타입: {}", beanCreator.getSupportedType());
    }

    @Test
    @Order(2)
    @DisplayName("지원되는 AWS 서비스 목록 확인")
    void testSupportedServices() {
        Set<LocalStackContainerSpec.AwsService> supportedServices = beanCreator.getSupportedServices();
        
        assertNotNull(supportedServices, "지원되는 서비스 목록이 null이면 안됩니다");
        
        if (supportedServices.isEmpty()) {
            log.info("⚠️ AWS SDK 의존성이 없어서 지원되는 서비스가 없습니다");
        } else {
            log.info("✅ 지원되는 AWS 서비스들: {}", supportedServices);
        }
        
        for (LocalStackContainerSpec.AwsService service : Set.of(
                LocalStackContainerSpec.AwsService.S3,
                LocalStackContainerSpec.AwsService.DYNAMODB,
                LocalStackContainerSpec.AwsService.SQS,
                LocalStackContainerSpec.AwsService.SNS,
                LocalStackContainerSpec.AwsService.LAMBDA
        )) {
            if (beanCreator.isServiceSupported(service)) {
                log.info("✅ {} 서비스가 지원됩니다 (AWS SDK 사용 가능)", service);
            } else {
                log.info("⚠️ {} 서비스를 건너뜁니다 (AWS SDK 의존성 없음)", service);
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("AWS 서비스별 팩토리 가져오기 테스트")
    void testGetFactory() {
        for (LocalStackContainerSpec.AwsService service : LocalStackContainerSpec.AwsService.values()) {
            var factoryOpt = beanCreator.getFactory(service);
            
            if (factoryOpt.isPresent()) {
                var factory = factoryOpt.get();
                assertEquals(service, factory.getSupportedService(), 
                    "팩토리의 지원 서비스가 일치해야 합니다");
                assertNotNull(factory.getBeanName(), "빈 이름이 null이면 안됩니다");
                assertFalse(factory.getBeanName().trim().isEmpty(), "빈 이름이 비어있으면 안됩니다");
                
                log.debug("✅ {} 서비스 팩토리: {} -> 빈 이름: '{}'", 
                    service, factory.getClass().getSimpleName(), factory.getBeanName());
            } else {
                log.debug("⚠️ {} 서비스 팩토리를 찾을 수 없습니다 (의존성 없음)", service);
            }
        }
    }

    @Test
    @Order(4)
    @DisplayName("잘못된 컨테이너 타입에 대한 예외 처리")
    void testInvalidContainerType() {
        var mockContainer = new org.testcontainers.containers.GenericContainer<>("redis:7-alpine");
        ContainerInfo invalidContainerInfo = new ContainerInfo(
                "invalid-container",
                ContainerType.REDIS, // 잘못된 타입
                mockContainer,
                spec
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> beanCreator.createBean(invalidContainerInfo));
        
        assertTrue(exception.getMessage().contains("LocalStackContainer가 필요합니다"), 
                "예외 메시지가 적절해야 합니다");
        
        log.info("✅ 잘못된 컨테이너 타입에 대한 예외 처리 확인: {}", exception.getMessage());
    }

    @Test
    @Order(5)
    @DisplayName("잘못된 스펙 타입에 대한 예외 처리")
    void testInvalidSpecType() {
        var invalidSpec = new com.genius.primavera.testcontainers.config.RedisContainerSpec();
        ContainerInfo invalidSpecInfo = new ContainerInfo(
                "invalid-spec",
                ContainerType.LOCALSTACK,
                container,
                invalidSpec // 잘못된 스펙
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> beanCreator.createBean(invalidSpecInfo));
        
        assertTrue(exception.getMessage().contains("LocalStackContainerSpec이 필요합니다"), 
                "예외 메시지가 적절해야 합니다");
        
        log.info("✅ 잘못된 스펙 타입에 대한 예외 처리 확인: {}", exception.getMessage());
    }

    @Test
    @Order(6)
    @DisplayName("AWS 서비스가 없을 때 기본 서비스 사용")
    void testDefaultServicesWhenEmpty() {
        LocalStackContainerSpec emptySpec = new LocalStackContainerSpec();
        emptySpec.setServices(Set.of()); // 빈 서비스 목록
        
        ContainerInfo containerInfo = new ContainerInfo(
                "empty-services",
                ContainerType.LOCALSTACK,
                container,
                emptySpec
        );

        Object result = assertDoesNotThrow(() -> beanCreator.createBean(containerInfo));
        
        assertNotNull(result, "결과가 null이면 안됩니다");
        
        if (result instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> clientMap = (Map<String, Object>) result;
            log.info("✅ 기본 서비스 사용 시 생성된 클라이언트들: {}", clientMap.keySet());
        } else {
            log.info("✅ 기본 서비스 사용 결과: {}", result.getClass().getSimpleName());
        }
    }

    @Test
    @Order(7)
    @DisplayName("빈 생성기가 올바르게 등록되었는지 확인")
    void testBeanCreatorRegistration() {
        var creatorOpt = com.genius.primavera.testcontainers.bean.BeanCreatorRegistry
                .findCreator(ContainerType.LOCALSTACK);
        
        assertTrue(creatorOpt.isPresent(), "LocalStackBeanCreator가 등록되어야 합니다");
        assertInstanceOf(LocalStackBeanCreator.class, creatorOpt.get(), 
                "등록된 creator가 LocalStackBeanCreator 인스턴스여야 합니다");
        
        log.info("✅ LocalStackBeanCreator가 BeanCreatorRegistry에 올바르게 등록되었습니다");
    }

    @Test
    @Order(8)
    @DisplayName("여러 AWS 서비스 조합 테스트")
    void testMultipleServiceCombination() {
        Set<LocalStackContainerSpec.AwsService>[] serviceCombinations = new Set[]{
                Set.of(LocalStackContainerSpec.AwsService.S3),
                Set.of(LocalStackContainerSpec.AwsService.S3, LocalStackContainerSpec.AwsService.DYNAMODB),
                Set.of(LocalStackContainerSpec.AwsService.SQS, LocalStackContainerSpec.AwsService.SNS),
                Set.of(LocalStackContainerSpec.AwsService.S3, LocalStackContainerSpec.AwsService.DYNAMODB, 
                       LocalStackContainerSpec.AwsService.SQS, LocalStackContainerSpec.AwsService.SNS,
                       LocalStackContainerSpec.AwsService.LAMBDA)
        };

        for (int i = 0; i < serviceCombinations.length; i++) {
            Set<LocalStackContainerSpec.AwsService> services = serviceCombinations[i];
            
            LocalStackContainerSpec testSpec = new LocalStackContainerSpec();
            testSpec.setServices(services);
            
            ContainerInfo containerInfo = new ContainerInfo(
                    "test-combination-" + i,
                    ContainerType.LOCALSTACK,
                    container,
                    testSpec
            );

            Object result = assertDoesNotThrow(() -> beanCreator.createBean(containerInfo), 
                    "서비스 조합 " + services + " 처리 중 예외가 발생하면 안됩니다");
            
            assertNotNull(result, "결과가 null이면 안됩니다");
            
            if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> clientMap = (Map<String, Object>) result;
                log.info("✅ 서비스 조합 {} -> 생성된 클라이언트들: {}", 
                        services, clientMap.keySet());
            }
        }
    }

    @AfterAll
    void tearDown() {
        log.info("LocalStackBeanCreator 테스트가 완료되었습니다");
    }
}