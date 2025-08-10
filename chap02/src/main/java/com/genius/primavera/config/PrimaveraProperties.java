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
        @NotBlank(message = "translated_text_7 translated_text_6 translated_text_5")
        private String username;

        @NotBlank(message = "translated_text_7 translated_text_5 translated_text_5")
        @Size(min = 8, message = "translated_text_5 translated_text_2 8translated_text_1 translated_text_5 translated_text_3")
        private String password;

        @NotBlank(message = "translated_text_7 URLtranslated_text_1 translated_text_5")
        @Pattern(regexp = "^jdbc:.*", message = "translated_text_3 JDBC URL translated_text_3 translated_text_4")
        private String url;

        @NotEmpty(message = "translated_text_2 translated_text_3 translated_text_3 translated_text_3 translated_text_3")
        private List<String> tables = new ArrayList<>();
    }

    @Data
    public static class Search {
        private Params params = new Params();

        @Data
        public static class Params {
            private String keyword;

            @Min(value = 1, message = "translated_text_3 translated_text_3 1 translated_text_5 translated_text_3")
            private Integer page = 1;

            private String sort = "asc";

            private Boolean enableHighlight = false;
        }

        private List<String> engines = new ArrayList<>();
    }

    @Data
    public static class User {
        private Long id;

        @NotBlank(message = "translated_text_3translated_text_1 translated_text_5")
        @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "translated_text_3 translated_text_3 translated_text_3 translated_text_4")
        private String email;

        private String role = "USER";

        private boolean active = true;
    }

    @Data
    public static class Cache {
        private boolean enabled = true;
        private Duration timeToLive = Duration.ofMinutes(5);
        private String type = "local";

        @Min(value = 1, message = "translated_text_2 translated_text_2 translated_text_2 1 translated_text_5 translated_text_3")
        private int maxEntries = 1000;
    }
}
