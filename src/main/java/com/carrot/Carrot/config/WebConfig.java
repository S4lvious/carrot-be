package com.carrot.Carrot.config;

import com.carrot.Carrot.security.SubscriptionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
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
            public void addCorsMappings(@SuppressWarnings("null") CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("https://app.powerwebsoftware.it", "http://localhost", "http://localhost:4200")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }

            @Override
            public void addInterceptors(@SuppressWarnings("null") InterceptorRegistry registry) {
                registry.addInterceptor(subscriptionInterceptor)
                .excludePathPatterns("/api/auth/**","/stripe-webhook","/stripe-webhook/**")
                .addPathPatterns("/api/**");

            }
        };
    }
}
