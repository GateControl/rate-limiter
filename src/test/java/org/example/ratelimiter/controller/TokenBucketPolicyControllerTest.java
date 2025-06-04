package org.example.ratelimiter.controller;

import org.example.ratelimiter.SecurityConfig;
import org.example.ratelimiter.dto.TokenBucketPolicy;
import org.example.ratelimiter.dto.TokenBucketPolicyRequest;
import org.example.ratelimiter.dto.PolicyKey;
import org.example.ratelimiter.filter.AdminApiKeyFilter;
import org.example.ratelimiter.service.PolicyService;
import org.example.ratelimiter.service.RateLimiterService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.context.annotation.Import;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PolicyController.class)
@TestPropertySource(properties = "admin.api-key=test-key")
@Import({SecurityConfig.class})
class TokenBucketPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static RequestPostProcessor apiKey() {
        return request -> {
            request.addHeader("X-API-Key", "test-key");
            return request;
        };
    }

    @MockBean
    private PolicyService policyService;

    @MockBean
    private RateLimiterService rateLimiterService;

    @Test
    void createTokenBucketPolicyValidRequestReturnsCreated() throws Exception {
        when(policyService.policyExists("foo")).thenReturn(false);
        String json = """
                {"policyKey":{"route":"foo"},"capacity":10,"refillTokens":1,"refillIntervalMillis":1000}
                """;
        mockMvc.perform(post("/policies").with(apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        verify(policyService, times(1)).policyExists("route:foo");
        ArgumentCaptor<TokenBucketPolicy> captor = ArgumentCaptor.forClass(TokenBucketPolicy.class);
        verify(policyService, times(1)).createPolicy(captor.capture());
        TokenBucketPolicy created = captor.getValue();
        assertThat(created.getId()).isEqualTo("route:foo");
        assertThat(created.getCapacity()).isEqualTo(10);
        assertThat(created.getRefillTokens()).isEqualTo(1);
        assertThat(created.getRefillIntervalMillis()).isEqualTo(1000);
    }

    @Test
    void createTokenBucketPolicyMissingIdReturnsBadRequest() throws Exception {
        String json = """
                {"capacity":10,"refillTokens":1,"refillIntervalMillis":1000}
                """;
        mockMvc.perform(post("/policies").with(apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(policyService);
    }

    @Test
    void createTokenBucketPolicyBlankIdReturnsBadRequest() throws Exception {
        String json = """
                {"policyKey":{"route":""},"capacity":10,"refillTokens":1,"refillIntervalMillis":1000}
                """;
        mockMvc.perform(post("/policies").with(apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(policyService);
    }

    @Test
    void createTokenBucketPolicyInvalidCapacityReturnsBadRequest() throws Exception {
        String json = """
                {"policyKey":{"route":"foo"},"capacity":0,"refillTokens":1,"refillIntervalMillis":1000}
                """;
        mockMvc.perform(post("/policies").with(apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(policyService);
    }

    @Test
    void createTokenBucketPolicyInvalidRefillTokensReturnsBadRequest() throws Exception {
        String json = """
                {"policyKey":{"route":"foo"},"capacity":10,"refillTokens":0,"refillIntervalMillis":1000}
                """;
        mockMvc.perform(post("/policies").with(apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(policyService);
    }

    @Test
    void createTokenBucketPolicyInvalidRefillIntervalMillisReturnsBadRequest() throws Exception {
        String json = """
                {"policyKey":{"route":"foo"},"capacity":10,"refillTokens":1,"refillIntervalMillis":0}
                """;
        mockMvc.perform(post("/policies").with(apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(policyService);
    }
    @Test
    void createTokenBucketPolicyAlreadyExistsReturnsConflict() throws Exception {
        when(policyService.policyExists("route:foo")).thenReturn(true);
        String json = """
                {"policyKey":{"route":"foo"},"capacity":10,"refillTokens":1,"refillIntervalMillis":1000}
                """;
        mockMvc.perform(post("/policies").with(apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());

        verify(policyService, times(1)).policyExists("route:foo");
        verify(policyService, never()).createPolicy(any(TokenBucketPolicy.class));
    }
    @Test
    void updateTokenBucketPolicyMissingIdReturnsBadRequest() throws Exception {
        String json = """
                {"capacity":10,"refillTokens":1,"refillIntervalMillis":1000}
                """;
        mockMvc.perform(put("/policies").with(apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(policyService);
    }

    @Test
    void updateTokenBucketPolicyBlankIdReturnsBadRequest() throws Exception {
        String json = """
                {"policyKey":{"route":""},"capacity":10,"refillTokens":1,"refillIntervalMillis":1000}
                """;
        mockMvc.perform(put("/policies").with(apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(policyService);
    }

    @Test
    void updateTokenBucketPolicyValidRequestReturnsOk() throws Exception {
        when(policyService.policyExists("route:foo")).thenReturn(true);
        String json = """
                {"policyKey":{"route":"foo"},"capacity":10,"refillTokens":1,"refillIntervalMillis":1000}
                """;
        mockMvc.perform(put("/policies").with(apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        ArgumentCaptor<TokenBucketPolicy> captor = ArgumentCaptor.forClass(TokenBucketPolicy.class);
        verify(policyService, times(1)).policyExists("route:foo");
        verify(policyService, times(1)).updatePolicy(captor.capture());
        TokenBucketPolicy updated = captor.getValue();
        assertThat(updated.getId()).isEqualTo("route:foo");
        assertThat(updated.getCapacity()).isEqualTo(10);
        assertThat(updated.getRefillTokens()).isEqualTo(1);
        assertThat(updated.getRefillIntervalMillis()).isEqualTo(1000);
    }

    @Test
    void updateTokenBucketPolicyNonExistingReturnsNotFound() throws Exception {
        when(policyService.policyExists("route:foo")).thenReturn(false);
        String json = """
                {"policyKey":{"route":"foo"},"capacity":10,"refillTokens":1,"refillIntervalMillis":1000}
                """;
        mockMvc.perform(put("/policies").with(apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());

        verify(policyService, times(1)).policyExists("route:foo");
        verify(policyService, never()).updatePolicy(any(TokenBucketPolicy.class));
    }

    @Test
    void getTokenBucketPoliciesReturnsMappedResponse() throws Exception {
        when(policyService.getPolicies()).thenReturn(List.of(
                new TokenBucketPolicy("route:foo", 1, 2, 3),
                new TokenBucketPolicy("route:bar", 4, 5, 6)
        ));
        String json = """
                [
                  {"policyKey":{"route":"foo"},"capacity":1,"refillTokens":2,"refillIntervalMillis":3},
                  {"policyKey":{"route":"bar"},"capacity":4,"refillTokens":5,"refillIntervalMillis":6}
                ]
                """;
        mockMvc.perform(get("/policies").with(apiKey()))
                .andExpect(status().isOk())
                .andExpect(content().json(json, false));
    }
}