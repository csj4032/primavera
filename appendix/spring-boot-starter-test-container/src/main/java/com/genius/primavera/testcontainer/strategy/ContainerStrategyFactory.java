package com.genius.primavera.testcontainer.strategy;

import java.util.HashMap;
import java.util.Map;

/**
 * 컨테이너 전략들을 관리하는 팩토리 클래스
 */
public class ContainerStrategyFactory {
    
    private static final Map<String, ContainerStrategy> strategies = new HashMap<>();
    
    static {
        registerStrategy(new MariaDBContainerStrategy());
        registerStrategy(new MySQLContainerStrategy());
        registerStrategy(new PostgreSQLContainerStrategy());
        registerStrategy(new RedisContainerStrategy());
        registerStrategy(new KafkaContainerStrategy());
        registerStrategy(new ElasticsearchContainerStrategy());
    }
    
    private static void registerStrategy(ContainerStrategy strategy) {
        strategies.put(strategy.getSupportedType().toLowerCase(), strategy);
        
        // PostgreSQL의 경우 "postgres" 별칭도 등록
        if ("postgresql".equals(strategy.getSupportedType())) {
            strategies.put("postgres", strategy);
        }
    }
    
    /**
     * 컨테이너 타입에 해당하는 전략을 반환합니다.
     * 
     * @param containerType 컨테이너 타입
     * @return 해당하는 전략, 없으면 null
     */
    public static ContainerStrategy getStrategy(String containerType) {
        return strategies.get(containerType.toLowerCase());
    }
    
    /**
     * 지원되는 컨테이너 타입들을 반환합니다.
     * 
     * @return 지원되는 컨테이너 타입들
     */
    public static String[] getSupportedTypes() {
        return strategies.keySet().toArray(new String[0]);
    }
    
    /**
     * 컨테이너 타입이 지원되는지 확인합니다.
     * 
     * @param containerType 컨테이너 타입
     * @return 지원 여부
     */
    public static boolean isSupported(String containerType) {
        return strategies.containsKey(containerType.toLowerCase());
    }
}