package com.genius.primavera.application;

import com.genius.primavera.domain.User;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PrimaveraService {

    private final RestTemplate restTemplate;

    public PrimaveraService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.messageConverters(new MappingJackson2HttpMessageConverter()).build();
    }

    public User getUser(long id) {
        return this.restTemplate.getForObject("http://localhost:8080/users/" + id, User.class);
    }
}
