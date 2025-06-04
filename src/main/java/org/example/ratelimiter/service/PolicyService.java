package org.example.ratelimiter.service;

import org.example.ratelimiter.dto.TokenBucketPolicy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import static org.example.ratelimiter.service.PolicyId.TOKEN_BUCKET_POLICY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PolicyService {
    private final RedisTemplate<String, String> redisTemplate;

    public PolicyService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void updatePolicy(TokenBucketPolicy policy) {
        createPolicy(policy);
    }

    public boolean policyExists(String id) {
        String key = TOKEN_BUCKET_POLICY.key(id);
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    public void createPolicy(TokenBucketPolicy policy) {
        String key = TOKEN_BUCKET_POLICY.key(policy.getId());
        Map<String, String> values = new HashMap<>();
        values.put("capacity", String.valueOf(policy.getCapacity()));
        values.put("refillTokens", String.valueOf(policy.getRefillTokens()));
        values.put("refillIntervalMillis", String.valueOf(policy.getRefillIntervalMillis()));
        values.put("tokens", String.valueOf(policy.getCapacity()));
        values.put("timestamp", String.valueOf(System.currentTimeMillis()));
        redisTemplate.opsForHash().putAll(key, values);
    }

    /**
     * Retrieves all token bucket policies, excluding the "metrics" stream entry.
     */
    public List<TokenBucketPolicy> getPolicies() {
        String prefix = TOKEN_BUCKET_POLICY.getPrefix() + ':';
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        List<TokenBucketPolicy> policies = new ArrayList<>();
        for (String key : keys) {
            if (key.equals(TOKEN_BUCKET_POLICY.key("metrics"))) {
                continue;
            }
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries == null || entries.isEmpty()) {
                continue;
            }
            String id = key.substring(prefix.length());
            long capacity = Long.parseLong((String) entries.get("capacity"));
            long refillTokens = Long.parseLong((String) entries.get("refillTokens"));
            long refillIntervalMillis = Long.parseLong((String) entries.get("refillIntervalMillis"));
            policies.add(new TokenBucketPolicy(id, capacity, refillTokens, refillIntervalMillis));
        }
        return policies;
    }
}