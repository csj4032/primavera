package com.genius.primavera.testcontainer.config;

import com.genius.primavera.testcontainer.ContainerType;
import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * @EnableTestContainers 어노테이션에 지정된 컨테이너 타입에 따라
 * 적절한 Configuration 클래스들을 동적으로 선택하는 Selector
 */
@Slf4j
public class ContainerConfigurationSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        // @EnableTestContainers 어노테이션 정보 추출
        var enableTestContainersAttrs = importingClassMetadata.getAnnotationAttributes(
            EnableTestContainers.class.getName()
        );
        
        if (enableTestContainersAttrs == null) {
            log.warn("@EnableTestContainers 어노테이션을 찾을 수 없습니다. 기본 MariaDB 컨테이너를 사용합니다.");
            return new String[]{MariaDBContainerConfiguration.class.getName()};
        }
        
        // 컨테이너 타입 배열 추출
        ContainerType[] containerTypes = (ContainerType[]) enableTestContainersAttrs.get("containers");
        String initScript = (String) enableTestContainersAttrs.get("initScript");
        boolean reuse = (Boolean) enableTestContainersAttrs.get("reuse");
        
        log.info("TestContainers 설정 - 컨테이너 타입: {}, 초기화 스크립트: {}, 재사용: {}", 
                 java.util.Arrays.toString(containerTypes), initScript, reuse);
        
        List<String> configurationClasses = new ArrayList<>();
        
        // 각 컨테이너 타입에 따른 Configuration 클래스 선택
        for (ContainerType containerType : containerTypes) {
            String configurationClass = getConfigurationClass(containerType);
            if (configurationClass != null) {
                configurationClasses.add(configurationClass);
                log.info("컨테이너 타입 {} 에 대한 Configuration 클래스 추가: {}", 
                         containerType, configurationClass);
            } else {
                log.warn("지원되지 않는 컨테이너 타입: {}", containerType);
            }
        }
        
        // 공통 Configuration도 추가
        configurationClasses.add(TestContainerCommonConfiguration.class.getName());
        
        return configurationClasses.toArray(new String[0]);
    }
    
    /**
     * 컨테이너 타입에 따른 Configuration 클래스명 반환
     */
    private String getConfigurationClass(ContainerType containerType) {
        return switch (containerType) {
            case MARIADB -> MariaDBContainerConfiguration.class.getName();
            case MYSQL -> MySQLContainerConfiguration.class.getName();
            case POSTGRESQL -> PostgreSQLContainerConfiguration.class.getName();
            case REDIS -> RedisContainerConfiguration.class.getName();
            case MONGODB -> MongoDBContainerConfiguration.class.getName();
            case ELASTICSEARCH -> ElasticsearchContainerConfiguration.class.getName();
            case KAFKA -> KafkaContainerConfiguration.class.getName();
            default -> {
                log.error("알 수 없는 컨테이너 타입: {}", containerType);
                yield null;
            }
        };
    }
}