package org.example.ratelimiter.dto;

public class TokenBucketPolicy extends RateLimitPolicy {
    private long capacity;
    private long refillTokens;
    private long refillIntervalMillis;

    public TokenBucketPolicy() {
    }

    public TokenBucketPolicy(String id, long capacity, long refillTokens, long refillIntervalMillis) {
        super(id);
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillIntervalMillis = refillIntervalMillis;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getRefillTokens() {
        return refillTokens;
    }

    public void setRefillTokens(long refillTokens) {
        this.refillTokens = refillTokens;
    }

    public long getRefillIntervalMillis() {
        return refillIntervalMillis;
    }

    public void setRefillIntervalMillis(long refillIntervalMillis) {
        this.refillIntervalMillis = refillIntervalMillis;
    }
}