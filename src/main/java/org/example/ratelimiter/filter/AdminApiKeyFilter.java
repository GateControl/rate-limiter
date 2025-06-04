package org.example.ratelimiter.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class AdminApiKeyFilter extends OncePerRequestFilter {

    private String plaintextKey;

    public AdminApiKeyFilter(String key) {
        this.plaintextKey = key;
    }

    private String cachedSha = null;                 // lazy-init hash once

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws IOException, ServletException {
        String presented = req.getHeader("X-API-Key");
        if (presented == null || !isValid(presented)) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API key");
            return;
        }

        // Mark caller as ADMIN for @PreAuthorize
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        chain.doFilter(req, res);
    }

    private boolean isValid(String presented) {
        // Compare SHA-256 so we never keep the plaintext in memory longer than needed
        if (cachedSha == null) {
            cachedSha = DigestUtils.sha256Hex(plaintextKey);
        }
        return MessageDigest.isEqual(
                cachedSha.getBytes(StandardCharsets.UTF_8),
                DigestUtils.sha256Hex(presented).getBytes(StandardCharsets.UTF_8)
        );
    }
}