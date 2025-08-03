package com.genius.primavera.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "lucy-filter")
public class LucyFilterDelegatingProperties {
    private boolean enabled = true;
    private String name = "xssEscapeServletFilter";
    private int order = 1;
    private String[] addUrlPatterns = {"/*"};

    public void setAddUrlPatterns(String urlPatterns) {
        if (urlPatterns != null && !urlPatterns.trim().isEmpty()) {
            this.addUrlPatterns = urlPatterns.split(",");
            for (int i = 0; i < this.addUrlPatterns.length; i++) {
                this.addUrlPatterns[i] = this.addUrlPatterns[i].trim();
            }
        }
    }

    public void setAddUrlPatterns(String[] urlPatterns) {
        this.addUrlPatterns = urlPatterns;
    }
}