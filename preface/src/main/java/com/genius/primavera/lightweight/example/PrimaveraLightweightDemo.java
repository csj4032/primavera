package com.genius.primavera.lightweight.example;

import com.genius.primavera.lightweight.framework.PrimaveraApplication;
import com.genius.primavera.lightweight.framework.PrimaveraApplicationContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrimaveraLightweightDemo {

    public static void main(String[] args) {
        try {
            PrimaveraApplicationContext context = PrimaveraApplication.run(PrimaveraLightweightDemo.class, args);
            log.info(" translated_text_7 execution translated_text_4. translated_text_5 Ctrl+Ctranslated_text_1 translated_text_4.");
            Thread.currentThread().join();
        } catch (Exception e) {
            log.error("translated_text_6 execution translated_text_1 error translated_text_2", e);
            System.exit(1);
        }
    }
}