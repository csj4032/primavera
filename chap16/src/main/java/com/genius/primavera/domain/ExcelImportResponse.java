package com.genius.primavera.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ExcelImportResponse {
    private String name;
    private long size;
    private FileType mediaType;
    private String message;

    public ExcelImportResponse(String name, long size, FileType mediaType, String message) {
        this.name = name;
        this.size = size;
        this.mediaType = mediaType;
        this.message = message;
    }
}
