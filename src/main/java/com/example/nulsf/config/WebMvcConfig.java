package com.example.nulsf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // This ensures the path "C:/NULSFimage" is converted to a valid file URL
    Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    
    // Create the directory if it doesn't exist to prevent "Folder not found" errors
    try {
        if (Files.notExists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    } catch (IOException e) {
        System.err.println("Could not create directory at C:/NULSFimage: " + e.getMessage());
    }

    String location = "file:///" + uploadPath.toString() + "/";
    
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations(location);

    // Debugging line: Check your console to see if this matches C:/NULSFimage
    System.out.println("Serving images from: " + location);
}
}
