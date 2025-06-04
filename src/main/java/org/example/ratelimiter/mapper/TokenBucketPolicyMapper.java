package org.example.ratelimiter.mapper;

import org.example.ratelimiter.dto.PolicyKey;
import org.example.ratelimiter.dto.TokenBucketPolicy;
import org.example.ratelimiter.dto.TokenBucketPolicyResponse;

import java.util.Objects;

/**
 * Converts TokenBucketPolicy domain objects to response DTOs, parsing the Redis key back to a PolicyKey.
 */
public class TokenBucketPolicyMapper {

    /**
     * Maps a TokenBucketPolicy to a TokenBucketPolicyResponse, reconstructing the PolicyKey from the policy id.
     */
    public static TokenBucketPolicyResponse toResponse(TokenBucketPolicy policy) {
        PolicyKey policyKey = parsePolicyKey(policy.getId());
        return new TokenBucketPolicyResponse(
                policyKey,
                policy.getCapacity(),
                policy.getRefillTokens(),
                policy.getRefillIntervalMillis()
        );
    }

    private static PolicyKey parsePolicyKey(String id) {
        String route = null;
        String userId = null;
        String clientApp = null;
        String httpMethod = null;
        String ipAddress = null;
        String[] parts = Objects.requireNonNull(id, "Policy id cannot be null").split(":");
        for (int i = 0; i + 1 < parts.length; i += 2) {
            String key = parts[i];
            String value = parts[i + 1];
            switch (key) {
                case "route" -> route = value;
                case "user" -> userId = value;
                case "app" -> clientApp = value;
                case "httpMethod" -> httpMethod = value;
                case "ipAddress" -> ipAddress = value;
            }
        }
        return new PolicyKey(route, userId, clientApp, httpMethod, ipAddress);
    }
}