package com.genius.primavera.application;

import org.springframework.stereotype.Service;

@Service
public class WorldServiceImpl implements WorldService {

    @Override
    public String hello() {
        return "Hello";
    }

    @Override
    public String world() {
        return "World!!!";
    }
}

