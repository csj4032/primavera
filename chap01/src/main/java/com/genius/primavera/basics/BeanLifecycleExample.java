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
        log.info("1. Constructor called - Bean file creation");
        this.status = "CONSTRUCTED";
    }
    
    @PostConstruct
    public void postConstruct() {
        log.info("2. @PostConstruct called - dependency test completed should");
        this.status = "POST_CONSTRUCT";
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("3. InitializingBean.afterPropertiesSet() called");

    }
    
    @PreDestroy
    public void preDestroy() {
        log.info("4. @PreDestroy called - Bean test should");
        this.status = "PRE_DESTROY";
    }
    
    @Override
    public void destroy() throws Exception {
        log.info("5. DisposableBean.destroy() called");
        this.status = "DESTROYED";
    }
    
    public String getStatus() {
        return status;
    }
    
    public void doSomething() {
        log.info("Beanshould file connection Endpoint. test: {}", status);
    }
}