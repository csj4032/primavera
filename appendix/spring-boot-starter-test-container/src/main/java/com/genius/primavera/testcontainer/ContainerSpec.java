package com.genius.primavera.testcontainer;

import java.lang.annotation.*;

/**
 * 테스트 컨테이너의 상세 스펙을 정의하는 어노테이션
 * 
 * 각 컨테이너의 타입, 이름, 설정을 함께 정의하여
 * 다중 컨테이너 환경에서 각 컨테이너를 명확히 구분하고 설정
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ContainerSpec {
    
    /**
     * 컨테이너 타입 지정 (필수)
     */
    ContainerType type();
    
    /**
     * 컨테이너 이름 (필수)
     * Bean 이름 및 Qualifier로 사용됨
     * 예: "primary", "secondary", "cache", "analytics"
     */
    String name();
    
    /**
     * 초기화 스크립트 경로
     * 기본값: "sql/init.sql"
     */
    String initScript() default "sql/init.sql";
    
    /**
     * 컨테이너 재사용 여부
     * 기본값: false (재사용 안함)
     */
    boolean reuse() default false;
    
    /**
     * 컨테이너 포트 오버라이드
     * 기본값: -1 (ContainerType의 기본 포트 사용)
     */
    int port() default -1;
    
    /**
     * 데이터베이스 이름
     * 기본값: "primavera"
     */
    String databaseName() default "primavera";
    
    /**
     * 데이터베이스 사용자명
     * 기본값: "primavera"
     */
    String username() default "primavera";
    
    /**
     * 데이터베이스 비밀번호
     * 기본값: "primavera"
     */
    String password() default "primavera";
    
    /**
     * 컨테이너별 추가 라벨
     * 디버깅 및 모니터링 목적
     */
    String[] labels() default {};
}