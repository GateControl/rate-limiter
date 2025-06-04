package org.example.ratelimiter.config;

import java.nio.charset.StandardCharsets;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ReadOffset;

@Configuration
public class RedisStreamConfig {

    private final RedisConnectionFactory connectionFactory;
    private final byte[] streamKey;
    private final String groupName;

    public RedisStreamConfig(RedisConnectionFactory connectionFactory,
                             @Value("${app.metrics.redis.stream.key}") String streamKey,
                             @Value("${app.metrics.redis.stream.group}") String groupName) {
        this.connectionFactory = connectionFactory;
        this.streamKey = streamKey.getBytes(StandardCharsets.UTF_8);
        this.groupName = groupName;
    }

    @PostConstruct
    public void createConsumerGroup() {
        RedisConnection connection = connectionFactory.getConnection();
        try {
            connection.streamCommands().xGroupCreate(streamKey, groupName, ReadOffset.latest(), true);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message == null || !message.contains("BUSYGROUP")) {
            }
        } finally {
            connection.close();
        }
    }
}