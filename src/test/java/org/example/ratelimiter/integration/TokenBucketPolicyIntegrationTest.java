package org.example.ratelimiter.integration;

import org.example.ratelimiter.dto.TokenBucketPolicyRequest;
import org.example.ratelimiter.dto.PolicyKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.HttpHeaders;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestPropertySource(properties = "admin.api-key=test-key")
class TokenBucketPolicyIntegrationTest {

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:6.2.6")).withExposedPorts(6379).waitingFor(Wait.forListeningPort());

    @Container
    static GenericContainer<?> influxContainer = new GenericContainer<>(DockerImageName.parse("influxdb:2.7"))
        .withEnv("DOCKER_INFLUXDB_INIT_MODE", "setup")
        .withEnv("DOCKER_INFLUXDB_INIT_USERNAME", "test-user")
        .withEnv("DOCKER_INFLUXDB_INIT_PASSWORD", "test-password")
        .withEnv("DOCKER_INFLUXDB_INIT_ORG", "test-org")
        .withEnv("DOCKER_INFLUXDB_INIT_BUCKET", "test-bucket")
        .withEnv("DOCKER_INFLUXDB_INIT_ADMIN_TOKEN", "test-token")
        .withExposedPorts(8086)
        .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379));
        registry.add("app.metrics.influx.url", () -> "http://" + influxContainer.getHost() + ":" + influxContainer.getMappedPort(8086));
        registry.add("app.metrics.influx.token", () -> "test-token");
        registry.add("app.metrics.influx.org", () -> "test-org");
        registry.add("app.metrics.influx.bucket", () -> "test-bucket");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createTokenBucketPolicyAndIsAllowedBehavior() {
        String apiKey = "test-key";
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-Key", apiKey);
        String policyId = "/api/users/";
        PolicyKey key = new PolicyKey(policyId, null, null, null, null);
        TokenBucketPolicyRequest request = new TokenBucketPolicyRequest(key, 2, 1, 10000);

        HttpEntity<TokenBucketPolicyRequest> createEntity = new HttpEntity<>(request, headers);
        ResponseEntity<Void> createResponse = restTemplate.exchange(
                "/policies", HttpMethod.POST, createEntity, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        HttpEntity<PolicyKey> entity = new HttpEntity<>(key, headers);

        ResponseEntity<Boolean> firstResponse = restTemplate.exchange(
                "/rate-limit", HttpMethod.PUT, entity, Boolean.class);
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(firstResponse.getBody()).isTrue();

        ResponseEntity<Boolean> secondResponse = restTemplate.exchange(
                "/rate-limit", HttpMethod.PUT, entity, Boolean.class);
        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondResponse.getBody()).isTrue();

        ResponseEntity<Boolean> thirdResponse = restTemplate.exchange(
                "/rate-limit", HttpMethod.PUT, entity, Boolean.class);
        assertThat(thirdResponse.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(thirdResponse.getBody()).isFalse();
    }
}