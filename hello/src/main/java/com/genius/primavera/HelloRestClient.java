package com.genius.primavera;

import org.springframework.cloud.openfeign.FeignClient;

import feign.Param;
import feign.RequestLine;

@FeignClient(name = "primavera-api")
public interface HelloRestClient {

    @RequestLine("GET /greeting/{name}")
    String getGreetingName(@Param("name")  String name);
}