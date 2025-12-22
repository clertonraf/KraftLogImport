package com.kraftlog.pdfimport.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kraftlog.api")
@Data
public class KraftLogApiProperties {
    private String baseUrl;
    private Auth auth;

    @Data
    public static class Auth {
        private String username;
        private String password;
    }
}
