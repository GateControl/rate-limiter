package org.example.ratelimiter.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import com.influxdb.client.domain.WritePrecision;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.data.redis.connection.stream.StreamReadOptions;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;

@Service
public class PollMetricsService {

    private final RedisTemplate<String, String> redisTemplate;
    private final WriteApiBlocking writeApi;
    private final String streamKey;
    private final String groupName;
    private final String consumerName;

    public PollMetricsService(RedisTemplate<String, String> redisTemplate,
                              InfluxDBClient influxDBClient,
                              @Value("${app.metrics.redis.stream.key}") String streamKey,
                              @Value("${app.metrics.redis.stream.group}") String groupName,
                              @Value("${spring.application.name}") String consumerName) {
        this.redisTemplate = redisTemplate;
        this.writeApi = influxDBClient.getWriteApiBlocking();
        this.streamKey = streamKey;
        this.groupName = groupName;
        this.consumerName = consumerName;
    }

    @Scheduled(fixedRateString = "${app.metrics.poll.rate}")
    public void pollMetrics() {
        StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();
        Consumer consumer = Consumer.from(groupName, consumerName);
        StreamReadOptions options = StreamReadOptions.empty()
                .count(100).block(Duration.ofMillis(500));
        StreamOffset<String> offset = StreamOffset.create(streamKey, ReadOffset.lastConsumed());

        List<MapRecord<String, String, String>> messages = streamOps.read(consumer, options, offset);
        if (messages == null || messages.isEmpty()) {
            return;
        }
        for (MapRecord<String, String, String> record : messages) {
            Map<String, String> values = record.getValue();
            String policyId = values.get("policyId");
            Point point = Point.measurement("token_bucket_metrics")
                    .addTag("policyId", policyId)
                    .addField("allowed", Long.parseLong(values.get("allowed")))
                    .addField("tokens", Long.parseLong(values.get("tokens")))
                    .addField("capacity", Long.parseLong(values.get("capacity")))
                    .addField("refillTokens", Long.parseLong(values.get("refillTokens")))
                    .addField("refillIntervalMillis", Long.parseLong(values.get("refillIntervalMillis")))
                    .addField("refillPeriods", Long.parseLong(values.get("refillPeriods")))
                    .addField("allowCount", Long.parseLong(values.get("allowCount")))
                    .addField("throttleCount", Long.parseLong(values.get("throttleCount")))
                    .time(Instant.now(), WritePrecision.MS);
            writeApi.writePoint(point);
            streamOps.acknowledge(streamKey, groupName, record.getId());
        }
    }
}