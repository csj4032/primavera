package com.genius.primavera;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.ribbon.FeignRibbonClientAutoConfiguration;

@FeignClient(name="HelloService" , url="${primavera.api.url}", configuration = FeignRibbonClientAutoConfiguration.class)
public interface HelloService {

}
