package org.example.ratelimiter.dto;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PolicyKey {
    private String route;
    private String userId;
    private String clientApp;
    private String httpMethod;
    private String ipAddress;

    public PolicyKey(String route, String userId, String clientApp, String httpMethod, String ipAddress) {
        this.route      = route;
        this.userId     = userId;
        this.clientApp  = clientApp;
        this.httpMethod = httpMethod;
        this.ipAddress  = ipAddress;

        if (Stream.of(route, userId, clientApp, httpMethod, ipAddress)
                .allMatch(s -> s == null || s.isBlank())) {
            throw new IllegalArgumentException("At least one key component must be non-empty");
        }
    }

    /** Canonical Redis/In-memory key */
    public String toRedisKey() {
        return Stream.of(
                        nullable(route == null || route.isEmpty() ? null : "route:" + route),
                        nullable(userId == null || userId.isEmpty() ? null : "user:" + userId),
                        nullable(clientApp == null || clientApp.isEmpty() ? null : "app:" + clientApp),
                        nullable(httpMethod == null || httpMethod.isEmpty() ? null : "httpMethod:" + httpMethod),
                        nullable(ipAddress == null || ipAddress.isEmpty() ? null : "ipAddress:" + ipAddress))
                .filter(Objects::nonNull)
                .collect(Collectors.joining(":"));
    }

    private static String nullable(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getClientApp() {
        return clientApp;
    }

    public void setClientApp(String clientApp) {
        this.clientApp = clientApp;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
