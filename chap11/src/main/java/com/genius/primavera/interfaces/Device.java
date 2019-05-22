package com.genius.primavera.interfaces;

import java.util.EnumMap;

import lombok.Getter;

@Getter
public enum Device {
    PC(" https://front-qa.wemakeprice.com"),
    MOBILE("https://mw-qa.wemakeprice.com");

    String domain;

    Device(String domain) {
        this.domain = domain;
    }

    PROMOTION(PC, MOBILE),
    NONE(PC, MOBILE),
    EVENT(PC, MOBILE);

    private Device pc;
    private Device mobile;

    Sample(Device pc, Device mobile) {
        this.pc = pc;
        this.mobile = mobile;
    }

    public static void main(String[] args) {
        //Sample.of("PROMOTION", "PC");
        EnumMap<Sample, String> a = new EnumMap(Sample.class);
        System.out.println(a);
    }

    private static void of(String type, String device) {
        //Map m = Stream.of(values()).collect(groupingBy(t-> t.getPc(), ()-> new EnumMap<>(Device.class), toList()));
        //System.out.println(m);

    }
}