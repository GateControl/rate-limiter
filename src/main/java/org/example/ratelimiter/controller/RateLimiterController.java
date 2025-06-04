package org.example.ratelimiter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.example.ratelimiter.dto.PolicyKey;
import org.example.ratelimiter.service.PolicyService;
import org.example.ratelimiter.service.RateLimiterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rate-limit")
public class RateLimiterController {

    private RateLimiterService rateLimiterService;
    private PolicyService policyService;

    public RateLimiterController(RateLimiterService rateLimiterService, PolicyService policyService) {
        this.rateLimiterService = rateLimiterService;
        this.policyService = policyService;
    }

    @Operation(summary = "Checks if request is allowed", description = "Runs token bucket algorithm using redis and lua")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description="Request is allowed"),
            @ApiResponse(responseCode = "429", description="Rate limit exceeded"),
            @ApiResponse(responseCode = "400", description="Key passed is an invalid key")
    })
    @PutMapping
    public ResponseEntity<Boolean> isAllowed(@RequestBody PolicyKey policyKey) {
        if (policyKey == null
                || policyKey.getRoute() == null
                || policyKey.getRoute().trim().isEmpty()) {
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
        String id = policyKey.toRedisKey();
        if (id.trim().isEmpty() || !policyService.policyExists(id)) {
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }

        boolean allowed = rateLimiterService.isAllowed(id);
        if (!allowed) {
            return new ResponseEntity<>(false, HttpStatus.TOO_MANY_REQUESTS);
        }
        return ResponseEntity.ok(allowed);
    }
}
