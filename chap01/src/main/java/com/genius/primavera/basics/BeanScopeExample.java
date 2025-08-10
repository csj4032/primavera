package com.genius.primavera.basics;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
public class BeanScopeExample {

    @Getter
    @Component
    @Scope("singleton")
    public static class SingletonBean {
        private final String id = UUID.randomUUID().toString();

        public SingletonBean() {
            log.info("SingletonBean creation: {}", id);
        }
    }

    @Getter
    @Component
    @Scope("prototype")
    public static class PrototypeBean {
        private final String id = UUID.randomUUID().toString();

        public PrototypeBean() {
            log.info("PrototypeBean creation: {}", id);
        }
    }
}