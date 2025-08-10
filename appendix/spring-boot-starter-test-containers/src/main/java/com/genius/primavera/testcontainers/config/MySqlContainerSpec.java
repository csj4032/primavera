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
public class MySqlContainerSpec extends DatabaseContainerSpec {
    
    @Pattern(regexp = "^(utf8|utf8mb4|latin1|ascii|binary)$", message = "Invalid character set")
    private String characterSet = "utf8mb4";
    
    @NotBlank(message = "Collation cannot be blank")
    private String collation = "utf8mb4_unicode_ci";
    
    @Size(min = 4, message = "Root password must be at least 4 characters")
    private String rootPassword = "root";
    
    private Boolean binlogEnabled = false;
    
    @Min(value = 64, message = "Buffer pool size must be at least 64MB")
    private Integer innodbBufferPoolSize = 128;
    
    private SqlMode sqlMode = SqlMode.STRICT_TRANS_TABLES;
    
    private StorageEngine defaultStorageEngine = StorageEngine.INNODB;
    
    @Min(value = 10, message = "Max connections must be at least 10")
    @Max(value = 100000, message = "Max connections must not exceed 100000")
    private Integer maxConnections = 151;
    
    @Min(value = 0, message = "Thread cache size must be non-negative")
    private Integer threadCacheSize = 9;
    
    private Boolean slowQueryLogEnabled = false;
    
    private Boolean generalLogEnabled = false;
    
    @Min(value = 1, message = "Server ID must be at least 1")
    private Integer serverId = 1;
    
    @Deprecated(since = "MySQL 8.0", forRemoval = true)
    private Boolean queryCacheEnabled = false;
    
    private Boolean sslEnabled = false;
    
    private String defaultTimeZone = "Asia/Seoul";
    
    public enum SqlMode {
        STRICT_TRANS_TABLES,
        TRADITIONAL,
        ONLY_FULL_GROUP_BY
    }
    
    public enum StorageEngine {
        MYISAM,
        CSV,
        INNODB
    }
}
