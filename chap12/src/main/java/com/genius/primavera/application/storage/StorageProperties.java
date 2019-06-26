package com.genius.primavera.application.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties("storage")
public class StorageProperties {
    private String location = "upload-dir";
}