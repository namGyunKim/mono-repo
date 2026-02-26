package com.example.global.config.web;

import com.example.global.version.ApiVersioning;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                .useRequestHeader(ApiVersioning.HEADER_NAME)
                .setDefaultVersion(ApiVersioning.DEFAULT_VERSION);
    }
}
