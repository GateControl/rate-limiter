package org.example.ratelimiter.service;

public enum PolicyId {
    TOKEN_BUCKET_POLICY("token-bucket-policy");

    private final String prefix;

    PolicyId(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public String key(String id) {
        return prefix + ":" + id;
    }
}