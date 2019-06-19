package com.genius.primavera;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrimaveraSpringBean {
    private String name;

    public PrimaveraSpringBean(String name) {
        this.name = name;
    }
}