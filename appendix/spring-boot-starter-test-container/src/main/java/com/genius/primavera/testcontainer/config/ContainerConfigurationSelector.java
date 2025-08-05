package com.genius.primavera.testcontainer.config;

import com.genius.primavera.testcontainer.ContainerType;
import com.genius.primavera.testcontainer.annotation.EnableTestContainers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @EnableTestContainers 어노테이션에 지정된 ContainerSpec에 따라
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
            log.warn("@EnableTestContainers 어노테이션을 찾을 수 없습니다. 기본 MariaDB primary 컨테이너를 사용합니다.");
            return new String[]{MariaDBContainerConfiguration.class.getName()};
        }
        
        // ContainerSpec 배열 추출
        AnnotationAttributes[] containerSpecs = (AnnotationAttributes[]) enableTestContainersAttrs.get("containers");
        log.info("TestContainers 설정 - 컨테이너 스펙 개수: {}", containerSpecs.length);
        
        List<String> configurationClasses = new ArrayList<>();
        
        // 컨테이너 타입별로 그룹화하여 처리
        Map<ContainerType, List<AnnotationAttributes>> containersByType = Arrays.stream(containerSpecs)
            .collect(Collectors.groupingBy(spec -> {
                Object typeObj = spec.get("type");
                if (typeObj instanceof ContainerType) {
                    return (ContainerType) typeObj;
                } else if (typeObj instanceof String) {
                    return ContainerType.valueOf((String) typeObj);
                } else {
                    throw new IllegalArgumentException("Unknown type format: " + typeObj);
                }
            }));
        
        // 각 컨테이너 타입에 따른 Configuration 클래스 선택
        for (Map.Entry<ContainerType, List<AnnotationAttributes>> entry : containersByType.entrySet()) {
            ContainerType containerType = entry.getKey();
            List<AnnotationAttributes> specs = entry.getValue();
            
            String configurationClass = getConfigurationClass(containerType);
            if (configurationClass != null) {
                configurationClasses.add(configurationClass);
                log.info("컨테이너 타입 {} 에 대한 Configuration 클래스 추가: {}", containerType, configurationClass);
                
                // 각 스펙 정보 로깅
                for (AnnotationAttributes spec : specs) {
                    String name = (String) spec.get("name");
                    String initScript = (String) spec.get("initScript");
                    boolean reuse = (Boolean) spec.get("reuse");
                    log.info("  - 컨테이너 스펙: name={}, initScript={}, reuse={}", name, initScript, reuse);
                }
            } else {
                log.warn("지원되지 않는 컨테이너 타입: {}", containerType);
            }
        }
        
        // 공통 Configuration도 추가
        configurationClasses.add(TestContainerCommonConfiguration.class.getName());
        
        // 어노테이션 정보를 시스템 프로퍼티로 저장 (Configuration에서 사용)
        storeContainerSpecsAsSystemProperties(containerSpecs);
        
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
    
    /**
     * ContainerSpec 정보를 시스템 프로퍼티로 저장
     * Configuration 클래스에서 참조할 수 있도록 함
     */
    private void storeContainerSpecsAsSystemProperties(AnnotationAttributes[] containerSpecs) {
        for (int i = 0; i < containerSpecs.length; i++) {
            AnnotationAttributes spec = containerSpecs[i];
            String prefix = "primavera.testcontainer.spec." + i;
            
            System.setProperty(prefix + ".type", spec.get("type").toString());
            System.setProperty(prefix + ".name", (String) spec.get("name"));
            System.setProperty(prefix + ".initScript", (String) spec.get("initScript"));
            System.setProperty(prefix + ".reuse", spec.get("reuse").toString());
            System.setProperty(prefix + ".port", spec.get("port").toString());
            System.setProperty(prefix + ".databaseName", (String) spec.get("databaseName"));
            System.setProperty(prefix + ".username", (String) spec.get("username"));
            System.setProperty(prefix + ".password", (String) spec.get("password"));
            
            String[] labels = (String[]) spec.get("labels");
            System.setProperty(prefix + ".labels", String.join(",", labels));
        }
        
        System.setProperty("primavera.testcontainer.spec.count", String.valueOf(containerSpecs.length));
        log.info("ContainerSpec 정보를 시스템 프로퍼티로 저장 완료: {} 개", containerSpecs.length);
    }
}