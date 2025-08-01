package com.genius.primavera.configuration;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * 국제화(i18n) 관련 설정을 담당하는 클래스입니다.
 * 
 * 주요 기능:
 * - 메시지 소스 설정 (messages.properties 파일 기반)
 * - 로케일 해결 전략 설정 (세션 기반)
 * - 로케일 변경 인터셉터 등록
 * - 기본 로케일을 한국어로 설정
 */
@Configuration
public class MessageConfig implements WebMvcConfigurer {

    /**
     * 국제화 메시지를 처리하는 MessageSource를 생성합니다.
     * 
     * 설정:
     * - 기본 메시지 파일: messages.properties (한국어)
     * - 다국어 지원: messages_en.properties (영어), messages_ja.properties (일본어)
     * - 문자 인코딩: UTF-8
     * - 캐시 설정: 개발 중에는 즉시 반영, 운영에서는 캐싱 활성화
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true); // 메시지가 없을 경우 키를 그대로 표시
        messageSource.setCacheSeconds(60); // 캐시 시간 설정 (60초)
        return messageSource;
    }

    /**
     * 로케일 해결 전략을 설정합니다.
     * 
     * SessionLocaleResolver 사용:
     * - 사용자의 로케일 정보를 세션에 저장
     * - 브라우저를 닫기 전까지 선택한 언어 유지
     * - 기본 로케일: 한국어 (Locale.KOREAN)
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.KOREAN);
        return localeResolver;
    }

    /**
     * URL 파라미터를 통한 로케일 변경을 처리하는 인터셉터입니다.
     * 
     * 사용법:
     * - ?lang=en : 영어로 변경
     * - ?lang=ko : 한국어로 변경  
     * - ?lang=ja : 일본어로 변경
     * 
     * 예시: http://localhost:8080/users?lang=en
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // URL 파라미터 이름
        return interceptor;
    }

    /**
     * 로케일 변경 인터셉터를 등록합니다.
     * 모든 요청에 대해 'lang' 파라미터를 확인하여 로케일을 변경합니다.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}