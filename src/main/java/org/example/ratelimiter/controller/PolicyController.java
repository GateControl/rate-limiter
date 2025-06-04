package org.example.ratelimiter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.example.ratelimiter.dto.TokenBucketPolicy;
import org.example.ratelimiter.dto.TokenBucketPolicyRequest;
import org.example.ratelimiter.dto.PolicyKey;
import org.example.ratelimiter.service.PolicyService;
import org.example.ratelimiter.service.RateLimiterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.ratelimiter.dto.TokenBucketPolicyResponse;
import org.example.ratelimiter.mapper.TokenBucketPolicyMapper;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/policies")
public class PolicyController {
    private final PolicyService policyService;
    private final RateLimiterService rateLimiterService;

    public PolicyController(PolicyService policyService, RateLimiterService rateLimiterService) {
        this.policyService = policyService;
        this.rateLimiterService = rateLimiterService;
    }

    @Operation(summary = "creates a token bucket policy", description = "policy will be set by a lua script in redis for use")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "policy was created successfully"),
            @ApiResponse(responseCode = "400", description = "the request body values were not set correctly"),
            @ApiResponse(responseCode = "409", description = "policy with given id already exists")
    })
    @PostMapping
    public ResponseEntity<Void> createPolicy(@RequestBody TokenBucketPolicyRequest request) {
        if (request.getPolicyKey() == null
                || request.getPolicyKey().getRoute() == null
                || request.getPolicyKey().getRoute().trim().isEmpty()
                || request.getPolicyKey().toRedisKey().trim().isEmpty()
                || request.getCapacity() < 1
                || request.getRefillTokens() < 1
                || request.getRefillIntervalMillis() < 1) {
            return ResponseEntity.badRequest().build();
        }
        String id = request.getPolicyKey().toRedisKey();
        if (policyService.policyExists(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        policyService.createPolicy(request.toPolicy());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "updates a token bucket policy", description = "updates an existing token bucket policy in redis")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "policy was updated successfully"),
            @ApiResponse(responseCode = "400", description = "the request body values were not set correctly"),
            @ApiResponse(responseCode = "404", description = "policy with given id does not exist")
    })
    @PutMapping
    public ResponseEntity<Void> updatePolicy(@RequestBody TokenBucketPolicyRequest request) {
        if (request.getPolicyKey() == null
                || request.getPolicyKey().getRoute() == null
                || request.getPolicyKey().getRoute().trim().isEmpty()
                || request.getPolicyKey().toRedisKey().trim().isEmpty()
                || request.getCapacity() < 1
                || request.getRefillTokens() < 1
                || request.getRefillIntervalMillis() < 1) {
            return ResponseEntity.badRequest().build();
        }
        String id = request.getPolicyKey().toRedisKey();
        if (!policyService.policyExists(id)) {
            return ResponseEntity.notFound().build();
        }
        policyService.updatePolicy(request.toPolicy());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "retrieves all token bucket policies", description = "retrieves all existing token bucket policies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "policies returned successfully")
    })
    @GetMapping
    public ResponseEntity<List<TokenBucketPolicyResponse>> getPolicies() {
        List<TokenBucketPolicyResponse> response = policyService.getPolicies().stream()
                .map(TokenBucketPolicyMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}