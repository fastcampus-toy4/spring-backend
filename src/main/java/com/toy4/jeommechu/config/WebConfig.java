package com.toy4.jeommechu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://react-frontend:80",
                        "http://localhost:80",
                        "http://155.248.175.96:3000",
                        "http://155.248.175.96:8000",
                        "http://155.248.175.96:9000"
                        )
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
