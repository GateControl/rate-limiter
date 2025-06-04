package org.example.ratelimiter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;

class PollMetricsServiceTest {

    private RedisTemplate<String, String> redisTemplate;
    private StreamOperations<String, String, String> streamOps;
    private InfluxDBClient influxDBClient;
    private WriteApiBlocking writeApi;
    private PollMetricsService pollMetricsService;

    private final String streamKey = "testStream";
    private final String groupName = "testGroup";
    private final String consumerName = "testConsumer";

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        streamOps = mock(StreamOperations.class);
        when(redisTemplate.opsForStream()).thenReturn((StreamOperations) streamOps);
        influxDBClient = mock(InfluxDBClient.class);
        writeApi = mock(WriteApiBlocking.class);
        when(influxDBClient.getWriteApiBlocking()).thenReturn(writeApi);
        pollMetricsService = new PollMetricsService(redisTemplate, influxDBClient, streamKey, groupName, consumerName);
    }

    @Test
    void pollMetricsShouldReadFromStreamWriteToInfluxAndAcknowledge() {
        Map<String, String> values = new HashMap<>();
        values.put("policyId", "policy123");
        values.put("allowed", "1");
        values.put("tokens", "5");
        values.put("timestamp", "1000");
        values.put("capacity", "10");
        values.put("refillTokens", "2");
        values.put("refillIntervalMillis", "1000");
        values.put("refillPeriods", "1");
        values.put("allowCount", "5");
        values.put("throttleCount", "0");

        MapRecord<String, String, String> record = MapRecord.create(streamKey, values)
                .withId(RecordId.of("123-0"));

        when(streamOps.read(eq(Consumer.from(groupName, consumerName)), any(org.springframework.data.redis.connection.stream.StreamReadOptions.class),
                eq(StreamOffset.create(streamKey, ReadOffset.lastConsumed()))))
                .thenReturn(List.of(record));

        pollMetricsService.pollMetrics();

        @SuppressWarnings("unchecked")
        var captor = org.mockito.ArgumentCaptor.forClass(Point.class);
        verify(writeApi).writePoint(captor.capture());
        Point point = captor.getValue();


//        assertThat(point.getMeasurement()).isEqualTo("token_bucket_metrics");
//        assertThat(point.getTags()).containsEntry("policyId", "policy123");
//        Map<String, Object> fields = point.getFields();
//        assertThat(fields.get("allowed")).isEqualTo(1L);
//        assertThat(fields.get("tokens")).isEqualTo(5L);
//        assertThat(fields.get("capacity")).isEqualTo(10L);
//        assertThat(fields.get("refillTokens")).isEqualTo(2L);
//        assertThat(fields.get("refillIntervalMillis")).isEqualTo(1000L);
//        assertThat(fields.get("refillPeriods")).isEqualTo(1L);
//        assertThat(fields.get("allowCount")).isEqualTo(5L);
//        assertThat(fields.get("throttleCount")).isEqualTo(0L);

        verify(streamOps).acknowledge(streamKey, groupName, record.getId());
    }

    @Test
    void pollMetricsShouldDoNothingWhenNoMessages() {
        when(streamOps.read(any(Consumer.class), any(org.springframework.data.redis.connection.stream.StreamReadOptions.class),
                any(StreamOffset.class))).thenReturn(Collections.emptyList());
        pollMetricsService.pollMetrics();
        verifyNoInteractions(writeApi);
    }
}