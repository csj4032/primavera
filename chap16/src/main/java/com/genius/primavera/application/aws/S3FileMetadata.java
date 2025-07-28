package com.genius.primavera.application.aws;

import java.time.Instant;

public record S3FileMetadata(
    String key,
    long size,
    Instant lastModified,
    String contentType,
    String etag
) {
}