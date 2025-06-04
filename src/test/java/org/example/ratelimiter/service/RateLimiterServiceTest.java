package org.example.ratelimiter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.ratelimiter.service.PolicyId.TOKEN_BUCKET_POLICY;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimiterServiceTest {

    private RedisTemplate<String, String> redisTemplate;
    private RedisScript<List> tokenBucketScript;
    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        tokenBucketScript = mock(RedisScript.class);
        rateLimiterService = new RateLimiterService(redisTemplate, tokenBucketScript, "testStream");
    }

    @Test
    void isAllowedReturnsTrueWhenScriptReturnsOne() {
        String policyId = "foo";
        String key = TOKEN_BUCKET_POLICY.key(policyId);
        when(redisTemplate.execute(eq(tokenBucketScript), eq(List.of(key, "testStream")), eq("1")))
                .thenReturn(List.of(1L));

        boolean allowed = rateLimiterService.isAllowed(policyId);

        assertThat(allowed).isTrue();
    }

    @Test
    void isAllowedReturnsFalseWhenScriptReturnsZero() {
        String policyId = "foo";
        String key = TOKEN_BUCKET_POLICY.key(policyId);
        when(redisTemplate.execute(eq(tokenBucketScript), eq(List.of(key, "testStream")), eq("1")))
                .thenReturn(List.of(0L));

        boolean allowed = rateLimiterService.isAllowed(policyId);

        assertThat(allowed).isFalse();
    }

    @Test
    void isAllowedReturnsFalseWhenScriptReturnsEmptyList() {
        String policyId = "foo";
        String key = TOKEN_BUCKET_POLICY.key(policyId);
        when(redisTemplate.execute(eq(tokenBucketScript), eq(List.of(key, "testStream")), eq("1")))
                .thenReturn(Collections.emptyList());

        boolean allowed = rateLimiterService.isAllowed(policyId);

        assertThat(allowed).isFalse();
    }

    @Test
    void isAllowedReturnsFalseWhenScriptReturnsNull() {
        String policyId = "foo";
        String key = TOKEN_BUCKET_POLICY.key(policyId);
        when(redisTemplate.execute(eq(tokenBucketScript), eq(List.of(key, "testStream")), eq("1")))
                .thenReturn(null);

        boolean allowed = rateLimiterService.isAllowed(policyId);

        assertThat(allowed).isFalse();
    }
}