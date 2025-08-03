package com.genius.primavera.testcontainer.v2;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * 다중 컨테이너를 지원하는 테스트 어노테이션
 * 각 컨테이너는 별도의 DataSource로 설정됩니다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(MultipleTestContainerExtension.class)
public @interface EnableMultipleTestContainers {
    
    /**
     * 컨테이너 설정 배열
     */
    ContainerDefinition[] containers();
    
    /**
     * 컨테이너 라이프사이클 모드
     */
    ContainerLifecycleMode lifecycleMode() default ContainerLifecycleMode.PER_CLASS;
    
    /**
     * 개별 컨테이너 정의
     */
    @interface ContainerDefinition {
        /**
         * 컨테이너 타입
         */
        ContainerType type();
        
        /**
         * 컨테이너 인스턴스 이름 (같은 타입의 여러 컨테이너 구분용)
         * 예: "redis1", "redis2", "mongo-primary", "mongo-secondary"
         */
        String instanceName();
        
        /**
         * DataSource 빈 이름 (DB 컨테이너용)
         */
        String dataSourceName() default "";
        
        /**
         * JdbcTemplate 빈 이름 (DB 컨테이너용)
         */
        String jdbcTemplateName() default "";
        
        /**
         * RedisTemplate 빈 이름 (Redis 컨테이너용)
         */
        String redisTemplateName() default "";
        
        /**
         * StringRedisTemplate 빈 이름 (Redis 컨테이너용)
         */
        String stringRedisTemplateName() default "";
        
        /**
         * MongoTemplate 빈 이름 (MongoDB 컨테이너용)
         */
        String mongoTemplateName() default "";
        
        /**
         * Primary 빈 여부
         */
        boolean primary() default false;
        
        /**
         * 데이터베이스명
         */
        String databaseName() default "";
        
        /**
         * 사용자명
         */
        String username() default "";
        
        /**
         * 비밀번호
         */
        String password() default "";
        
        /**
         * 포트 (고정 포트 사용 시)
         */
        int port() default 0;
        
        /**
         * 추가 환경 변수
         */
        String[] environmentVariables() default {};
    }
}