package com.example.studyroomreservation.global.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.base-path}")
    private String basePath;

    @Value("${file.upload.rooms-path}")
    private String roomsPath;

    // /rooms/** URL -> {file.upload.base-path}/{file.upload.rooms-path}/
    // 예) /rooms/21/main_xxx.jpg -> ./uploads/rooms/21/main_xxx.jpg
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        Path roomsDir = Path.of(basePath, roomsPath)
                .toAbsolutePath()
                .normalize();

        log.debug("#####[STATIC] /rooms/** -> {}", roomsDir.toUri());

        registry.addResourceHandler("/rooms/**")
                .addResourceLocations(roomsDir.toUri().toString());
    }
}
