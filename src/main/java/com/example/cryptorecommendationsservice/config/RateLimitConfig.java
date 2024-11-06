package com.example.cryptorecommendationsservice.config;

import com.example.cryptorecommendationsservice.filter.RateLimitingFilter;
import io.github.bucket4j.Bucket;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.time.Duration.ofSeconds;

@Configuration
public class RateLimitConfig {

    /**
     * Configures a Bucket4j bucket with a capacity of 20 tokens that refills at a rate of 20 tokens per minute.
     * Each token represents the capacity to handle one request.
     *
     * @return a configured Bucket instance.
     */
    @Bean
    public Bucket bucket() {

        return Bucket.builder()
                .addLimit(limit -> limit.capacity(20).refillGreedy(20, ofSeconds(60)).initialTokens(20))
                .build();
    }

    /**
     * Registers the RateLimitingFilter with the Spring context and applies it to the desired URL patterns.
     *
     * @param rateLimitingFilter the RateLimitingFilter bean.
     * @return a FilterRegistrationBean configured with the RateLimitingFilter.
     */
    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration(RateLimitingFilter rateLimitingFilter) {
        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>(rateLimitingFilter);
        registration.addUrlPatterns("/api/*"); // Apply the rate limiting filter to all API endpoints
        registration.setOrder(1); // Set the order if you have multiple filters
        return registration;
    }
}
