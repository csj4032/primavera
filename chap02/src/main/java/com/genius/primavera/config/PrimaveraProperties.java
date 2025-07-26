package com.genius.primavera.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "com.genius.primavera")
public class PrimaveraProperties {

    @Valid
    private Database database = new Database();

    @Valid
    private Search search = new Search();

    @Valid
    private List<User> users = new ArrayList<>();

    private Map<String, String> features = new HashMap<>();

    @Valid
    private Cache cache = new Cache();

    @Data
    public static class Database {
        @NotBlank(message = "데이터베이스 사용자명은 필수입니다")
        private String username;

        @NotBlank(message = "데이터베이스 비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        private String password;

        @NotBlank(message = "데이터베이스 URL은 필수입니다")
        @Pattern(regexp = "^jdbc:.*", message = "올바른 JDBC URL 형식이 아닙니다")
        private String url;

        @NotEmpty(message = "최소 하나의 테이블 설정이 필요합니다")
        private List<String> tables = new ArrayList<>();
    }

    @Data
    public static class Search {
        private Params params = new Params();

        @Data
        public static class Params {
            private String keyword;

            @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다")
            private Integer page = 1;

            private String sort = "asc";

            private Boolean enableHighlight = false;
        }

        private List<String> engines = new ArrayList<>();
    }

    @Data
    public static class User {
        private Long id;

        @NotBlank(message = "이메일은 필수입니다")
        @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "올바른 이메일 형식이 아닙니다")
        private String email;

        private String role = "USER";

        private boolean active = true;
    }

    @Data
    public static class Cache {
        private boolean enabled = true;
        private Duration timeToLive = Duration.ofMinutes(5);
        private String type = "local";

        @Min(value = 1, message = "최대 항목 수는 1 이상이어야 합니다")
        private int maxEntries = 1000;
    }
}
