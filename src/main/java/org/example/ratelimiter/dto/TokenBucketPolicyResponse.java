package org.example.ratelimiter.dto;

/**
 * Response payload for a token bucket policy, mirroring the request format.
 */
public class TokenBucketPolicyResponse {
    private PolicyKey policyKey;
    private long capacity;
    private long refillTokens;
    private long refillIntervalMillis;

    public TokenBucketPolicyResponse() {
    }

    public TokenBucketPolicyResponse(PolicyKey policyKey,
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
}