package com.example.cryptorecommendationsservice.filter;

import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.Duration.ofSeconds;

@Component
public class RateLimitingFilter implements Filter {

    // Map to hold buckets for each client IP
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        // Create a new bucket with capacity of 20 tokens, refilled every 60 seconds
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(20).refillGreedy(20, ofSeconds(60)).initialTokens(20))
                .build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String clientIp = request.getRemoteAddr(); // Get client IP address

        // Create a new bucket for the client IP if it doesn't already exist
        buckets.putIfAbsent(clientIp, createNewBucket());
        Bucket bucket = buckets.get(clientIp);

        // Check if the request can be processed
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response); // Forward the request if rate limit is not hit
        } else {
            ((HttpServletResponse) response).setStatus(429); // Return 429 if limit exceeded
        }
    }
}
