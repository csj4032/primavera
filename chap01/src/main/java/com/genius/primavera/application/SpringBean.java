package com.genius.primavera.application;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringBean {

    private String name;

    public SpringBean(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @PostConstruct
    public void construct() {
        log.info("construct");
    }

    @PreDestroy
    public void destroy() {
        log.info("destroy");
    }
}
