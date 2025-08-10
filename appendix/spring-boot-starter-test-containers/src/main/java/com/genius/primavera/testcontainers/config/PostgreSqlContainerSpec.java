package com.genius.primavera.testcontainers.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;

@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties
@Validated
public class PostgreSqlContainerSpec extends DatabaseContainerSpec {

    @NotBlank(message = "Locale cannot be blank")
    private String locale = "en_US.UTF-8";

    @Pattern(regexp = "^(UTF8|LATIN1|SQL_ASCII|EUC_KR|WIN1252)$", message = "Invalid encoding")
    private String encoding = "UTF8";

    @NotBlank(message = "Template database cannot be blank")
    private String templateDatabase = "template1";

    @Pattern(regexp = "^\\d+[kKmMgG][bB]?$", message = "Invalid shared buffers format (e.g., 128MB)")
    private String sharedBuffers = "128MB";

    @Pattern(regexp = "^\\d+[kKmMgG][bB]?$", message = "Invalid work mem format (e.g., 4MB)")
    private String workMem = "4MB";

    @Pattern(regexp = "^\\d+[kKmMgG][bB]?$", message = "Invalid maintenance work mem format (e.g., 64MB)")
    private String maintenanceWorkMem = "64MB";

    @Pattern(regexp = "^\\d+[kKmMgG][bB]?$", message = "Invalid WAL buffers format (e.g., 16MB)")
    private String walBuffers = "16MB";

    @Min(value = 1, message = "Checkpoint segments must be at least 1")
    @Max(value = 256, message = "Checkpoint segments must not exceed 256")
    private Integer checkpointSegments = 32;

    @Min(value = 10, message = "Max connections must be at least 10")
    @Max(value = 1000, message = "Max connections must not exceed 1000")
    private Integer maxConnections = 100;

    @Min(value = 0, message = "Log min duration must be non-negative")
    private Integer logMinDurationStatement;

    private Boolean autovacuum = true;

    private Boolean trackActivities = true;

    private Boolean trackStatements = false;

    private Boolean dataChecksums = true;

    private SslMode sslMode = SslMode.PREFER;

    private String timezone = "Asia/Seoul";

    private String dateStyle = "ISO";

    private String[] extensions = {};

    public enum PostgreSqlLogLevel {
        DEBUG5, DEBUG4, DEBUG3, DEBUG2, DEBUG1,
        INFO, NOTICE, WARNING, ERROR, LOG, FATAL, PANIC
    }

    public enum SslMode {
        DISABLE,
        ALLOW,
        PREFER,
        REQUIRE,
        VERIFY_CA,
        VERIFY_FULL
    }
}