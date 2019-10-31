package com.genius.primavera;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.ribbon.FeignRibbonClientAutoConfiguration;

import feign.Param;
import feign.RequestLine;

@FeignClient(name = "helloService", url = "${primavera.api.url}", configuration = FeignRibbonClientAutoConfiguration.class)
public interface HelloRestClient {

    @RequestLine("GET /greeting/{name}")
    String getGreetingName(@Param("name")  String name);
}