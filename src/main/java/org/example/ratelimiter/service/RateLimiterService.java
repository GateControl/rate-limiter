package org.example.ratelimiter.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import static org.example.ratelimiter.service.PolicyId.TOKEN_BUCKET_POLICY;

import java.util.List;

@Service
public class RateLimiterService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<List> tokenBucketScript;
    private final String metricsStreamKey;

    public RateLimiterService(RedisTemplate<String, String> redisTemplate,
                              RedisScript<List> tokenBucketScript,
                              @Value("${app.metrics.redis.stream.key}") String metricsStreamKey) {
        this.redisTemplate = redisTemplate;
        this.tokenBucketScript = tokenBucketScript;
        this.metricsStreamKey = metricsStreamKey;
    }

    public boolean isAllowed(String policyId) {
        String key = TOKEN_BUCKET_POLICY.key(policyId);
        List<Long> results = redisTemplate.execute(
                tokenBucketScript,
                List.of(key, metricsStreamKey),
                "1"
        );
        return results != null && !results.isEmpty() && results.get(0) == 1;
    }
}