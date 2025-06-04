package org.example.ratelimiter.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;
import com.influxdb.client.domain.WritePrecision;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.example.ratelimiter.service.PolicyId.TOKEN_BUCKET_POLICY;

/**
 * Scheduled service that polls current token counts for each token bucket policy
 * and writes these metrics to InfluxDB.
 */
@Service
public class CurrentTokensPollingService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<Long> currentTokenScript;
    private final WriteApiBlocking writeApi;
    private final String prefix;

    public CurrentTokensPollingService(RedisTemplate<String, String> redisTemplate,
                                       RedisScript<Long> currentTokenScript,
                                       InfluxDBClient influxDBClient) {
        this.redisTemplate = redisTemplate;
        this.currentTokenScript = currentTokenScript;
        this.writeApi = influxDBClient.getWriteApiBlocking();
        this.prefix = TOKEN_BUCKET_POLICY.getPrefix() + ":";
    }

    @Scheduled(fixedRateString = "${app.metrics.tokens.poll.rate}")
    public void pollCurrentTokens() {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (String key : keys) {
            if (key.equals("token-bucket-policy:metrics")) continue;
            Long tokens = redisTemplate.execute(currentTokenScript, List.of(key));
            if (tokens == null) {
                continue;
            }
            Point point = Point.measurement("token_bucket_current_tokens")
                    .addTag("policyId", key)
                    .addField("tokens", tokens)
                    .time(Instant.now(), WritePrecision.MS);
            writeApi.writePoint(point);
        }
    }
}