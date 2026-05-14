package com.kos.authentication.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig {

    @Value("${item.image.upload.path}")
    private String uploadPath;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Serve uploaded item images directly from the filesystem.
                // GET /restaurant-images/{restaurantId}/{fileName}
                // maps to  {item.image.upload.path}/{restaurantId}/{fileName}
                String location = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
                registry.addResourceHandler("/restaurant-images/**")
                        .addResourceLocations("file:" + location);
            }
        };
    }
}
