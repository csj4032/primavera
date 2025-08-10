package com.genius.primavera.lightweight.example.config;

import com.genius.primavera.lightweight.annotations.PrimaveraBean;
import com.genius.primavera.lightweight.annotations.PrimaveraConfiguration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@PrimaveraConfiguration
public class ExampleConfiguration {

    @PrimaveraBean
    public String applicationName() {
        String name = "Primavera Lightweight Framework Demo";
        log.info("applicationName Bean creation: {}", name);
        return name;
    }

    @PrimaveraBean
    public String applicationVersion() {
        String version = "1.0.0";
        log.info("applicationVersion Bean creation: {}", version);
        return version;
    }

    @PrimaveraBean("maxUsers")
    public Integer maxUserCount() {
        Integer maxUsers = 100;
        log.info("maxUsers Bean creation: {}", maxUsers);
        return maxUsers;
    }
}