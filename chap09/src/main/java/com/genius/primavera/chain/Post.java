package com.genius.primavera.chain;

import lombok.Data;

@Data
public class Post {
    private String to;
    private String from;
    private String message;
    private int step;
}