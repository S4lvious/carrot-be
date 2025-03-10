package com.carrot.Carrot.config;

import com.carrot.Carrot.security.SubscriptionInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableScheduling
public class WebConfig {

    private final SubscriptionInterceptor subscriptionInterceptor;

    @Autowired
    public WebConfig(SubscriptionInterceptor subscriptionInterceptor) {
        this.subscriptionInterceptor = subscriptionInterceptor;
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("https://app.powerwebsoftware.it", "http://localhost", "http://localhost:4200")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(subscriptionInterceptor)
                        .excludePathPatterns("/api/auth/**", "/stripe-webhook", "/stripe-webhook/**")
                        .addPathPatterns("/api/**");
            }
        };
    }

    /**
     * Definisce il bean RestTemplate, così da poterlo iniettare con @Autowired
     * ad esempio in FatturaService
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Abilita la gestione delle date Java 8
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // Disabilita la serializzazione in array
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true); // Serializza le date in ISO
        return mapper;
    }

}
