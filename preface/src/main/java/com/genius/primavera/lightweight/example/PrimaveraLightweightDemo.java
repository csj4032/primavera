package com.genius.primavera.lightweight.example;

import com.genius.primavera.lightweight.framework.PrimaveraApplication;
import com.genius.primavera.lightweight.framework.PrimaveraApplicationContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrimaveraLightweightDemo {

    public static void main(String[] args) {
        try {
            PrimaveraApplicationContext context = PrimaveraApplication.run(PrimaveraLightweightDemo.class, args);
            log.info(" logging execution file. Endpoint Ctrl+Cshould file.");
            Thread.currentThread().join();
        } catch (Exception e) {
            log.error("with execution failed with error", e);
            System.exit(1);
        }
    }
}