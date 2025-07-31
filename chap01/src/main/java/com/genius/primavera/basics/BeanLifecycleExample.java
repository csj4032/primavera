package com.genius.primavera.basics;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("singleton")
public class BeanLifecycleExample implements InitializingBean, DisposableBean {
    
    private String status = "NOT_INITIALIZED";
    
    public BeanLifecycleExample() {
        log.info("1. Constructor 호출 - Bean 인스턴스 생성");
        this.status = "CONSTRUCTED";
    }
    
    @PostConstruct
    public void postConstruct() {
        log.info("2. @PostConstruct 호출 - 의존성 주입 완료 후");
        this.status = "POST_CONSTRUCT";
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("3. InitializingBean.afterPropertiesSet() 호출");
        // POST_CONSTRUCT 상태를 유지하기 위해 status 변경하지 않음
    }
    
    @PreDestroy
    public void preDestroy() {
        log.info("4. @PreDestroy 호출 - Bean 소멸 전");
        this.status = "PRE_DESTROY";
    }
    
    @Override
    public void destroy() throws Exception {
        log.info("5. DisposableBean.destroy() 호출");
        this.status = "DESTROYED";
    }
    
    public String getStatus() {
        return status;
    }
    
    public void doSomething() {
        log.info("Bean이 비즈니스 로직을 수행합니다. 현재 상태: {}", status);
    }
}