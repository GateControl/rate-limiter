package org.example.ratelimiter.dto;

/**
 * Request payload for creating or updating a token bucket policy.
 */
public class TokenBucketPolicyRequest {
    private PolicyKey policyKey;
    private long capacity;
    private long refillTokens;
    private long refillIntervalMillis;

    public TokenBucketPolicyRequest() {
    }

    public TokenBucketPolicyRequest(PolicyKey policyKey,
                                    long capacity,
                                    long refillTokens,
                                    long refillIntervalMillis) {
        this.policyKey = policyKey;
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillIntervalMillis = refillIntervalMillis;
    }

    public PolicyKey getPolicyKey() {
        return policyKey;
    }

    public void setPolicyKey(PolicyKey policyKey) {
        this.policyKey = policyKey;
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

    /**
     * Maps this request to a TokenBucketPolicy, computing the policy id via PolicyKey.
     */
    public TokenBucketPolicy toPolicy() {
        String id = policyKey.toRedisKey();
        return new TokenBucketPolicy(id, capacity, refillTokens, refillIntervalMillis);
    }
}