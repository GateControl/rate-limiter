package org.example.ratelimiter.dto;

public class RateLimitPolicy {
    private String id;

    public RateLimitPolicy() {
    }

    public RateLimitPolicy(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}