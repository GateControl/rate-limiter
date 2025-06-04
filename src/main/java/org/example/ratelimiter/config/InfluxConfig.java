package org.example.ratelimiter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;

@Configuration
public class InfluxConfig {

    @Value("${app.metrics.influx.url}")
    private String url;

    @Value("${app.metrics.influx.token}")
    private String token;

    @Value("${app.metrics.influx.org}")
    private String org;

    @Value("${app.metrics.influx.bucket}")
    private String bucket;

    @Bean
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
    }
}