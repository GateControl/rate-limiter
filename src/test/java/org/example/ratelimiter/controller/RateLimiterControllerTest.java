package org.example.ratelimiter.controller;

import org.example.ratelimiter.SecurityConfig;
import org.example.ratelimiter.dto.PolicyKey;
import org.example.ratelimiter.service.PolicyService;
import org.example.ratelimiter.service.RateLimiterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RateLimiterController.class)
@TestPropertySource(properties = "admin.api-key=test-key")
@Import({SecurityConfig.class})
class RateLimiterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PolicyService policyService;

    @MockBean
    private RateLimiterService rateLimiterService;

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_KEY_VALUE = "test-key";

    @Test
    void isAllowedBlankRouteReturnsBadRequest() throws Exception {
        String json = """
                {"route":""}
                """;
        mockMvc.perform(put("/rate-limit")
                .header(API_KEY_HEADER, API_KEY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(policyService, rateLimiterService);
    }

    @Test
    void isAllowedMissingRouteReturnsBadRequest() throws Exception {
        String json = "{}";
        mockMvc.perform(put("/rate-limit")
                .header(API_KEY_HEADER, API_KEY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(policyService, rateLimiterService);
    }
}