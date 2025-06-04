package org.example.ratelimiter.service;

import org.example.ratelimiter.dto.TokenBucketPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.ratelimiter.service.PolicyId.TOKEN_BUCKET_POLICY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PolicyServiceTest {

    private RedisTemplate<String, String> redisTemplate;
    private HashOperations<String, Object, Object> hashOperations;
    private PolicyService policyService;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        hashOperations = mock(HashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        policyService = new PolicyService(redisTemplate);
    }

    @Test
    void createPolicyWithValidPolicyPutsAllValuesInRedisHash() {
        TokenBucketPolicy policy = new TokenBucketPolicy("foo", 10, 1, 1000);
        long before = System.currentTimeMillis();
        policyService.createPolicy(policy);
        long after = System.currentTimeMillis();

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(hashOperations, times(1)).putAll(keyCaptor.capture(), mapCaptor.capture());

        assertThat(keyCaptor.getValue()).isEqualTo(TOKEN_BUCKET_POLICY.key("foo"));
        Map<String, String> values = mapCaptor.getValue();
        assertThat(values)
                .containsEntry("capacity", "10")
                .containsEntry("refillTokens", "1")
                .containsEntry("refillIntervalMillis", "1000")
                .containsEntry("tokens", "10");
        assertThat(values).containsKey("timestamp");
        long timestamp = Long.parseLong(values.get("timestamp"));
        assertThat(timestamp).isBetween(before, after);
    }

    @Test
    void getPoliciesReturnsOnlyNonStreamPolicies() {
        String prefix = TOKEN_BUCKET_POLICY.getPrefix() + ':';
        String fooKey = prefix + "foo";
        String metricsKey = prefix + "metrics";
        String barKey = prefix + "bar";
        when(redisTemplate.keys(prefix + "*")).thenReturn(Set.of(fooKey, metricsKey, barKey));

        Map<Object, Object> fooMap = new HashMap<>();
        fooMap.put("capacity", "1");
        fooMap.put("refillTokens", "2");
        fooMap.put("refillIntervalMillis", "3");
        when(hashOperations.entries(fooKey)).thenReturn(fooMap);

        Map<Object, Object> barMap = new HashMap<>();
        barMap.put("capacity", "4");
        barMap.put("refillTokens", "5");
        barMap.put("refillIntervalMillis", "6");
        when(hashOperations.entries(barKey)).thenReturn(barMap);

        List<TokenBucketPolicy> policies = policyService.getPolicies();
        assertThat(policies).hasSize(2);
        assertThat(policies).extracting(TokenBucketPolicy::getId)
                .containsExactlyInAnyOrder("foo", "bar");
        assertThat(policies).filteredOn(p -> p.getId().equals("foo")).flatExtracting(TokenBucketPolicy::getCapacity, TokenBucketPolicy::getRefillTokens, TokenBucketPolicy::getRefillIntervalMillis)
                .containsExactly(1L, 2L, 3L);
        assertThat(policies).filteredOn(p -> p.getId().equals("bar")).flatExtracting(TokenBucketPolicy::getCapacity, TokenBucketPolicy::getRefillTokens, TokenBucketPolicy::getRefillIntervalMillis)
                .containsExactly(4L, 5L, 6L);
    }
}