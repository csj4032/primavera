package com.genius.primavera.testcontainers.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Validated
@ConfigurationProperties
@EqualsAndHashCode(callSuper = true)
public class MariaDBContainerSpec extends DatabaseContainerSpec {
    
    @Pattern(regexp = "^(utf8|utf8mb4|latin1|ascii|binary)$", message = "Invalid character set")
    private String characterSet = "utf8mb4";
    
    @NotBlank(message = "Collation cannot be blank")
    private String collation = "utf8mb4_unicode_ci";
    
    private Boolean binlogEnabled = false;
    
    @Min(value = 64, message = "Buffer pool size must be at least 64MB")
    private Integer innodbBufferPoolSize = 128;
    
    private SqlMode sqlMode = SqlMode.STRICT_TRANS_TABLES;
    
    private StorageEngine defaultStorageEngine = StorageEngine.INNODB;
    
    @Deprecated(since = "MariaDB 10.1.7", forRemoval = true)
    private Boolean queryCacheEnabled = false;
    
    @Min(value = 10, message = "Max connections must be at least 10")
    @Max(value = 100000, message = "Max connections must not exceed 100000")
    private Integer maxConnections = 151;
    
    @Min(value = 0, message = "Thread cache size must be non-negative")
    private Integer threadCacheSize = 9;
    
    @Size(min = 4, message = "Root password must be at least 4 characters")
    private String rootPassword = "root";
    
    private Boolean slowQueryLogEnabled = false;
    
    @Pattern(regexp = "^(classpath:|file:|http://|https://)?.*\\.(sql|sh)$", message = "Init script must be .sql or .sh file")
    private String initScript;
    
    @NotNull
    private List<@NotBlank String> command = new ArrayList<>();
    
    public enum SqlMode {
        STRICT_TRANS_TABLES,
        TRADITIONAL,
        ONLY_FULL_GROUP_BY
    }
    
    public enum StorageEngine {
        MYISAM,
        COLUMNSTORE,
        INNODB
    }
}
