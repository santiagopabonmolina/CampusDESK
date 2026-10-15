package com.spring.security.jwt;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Generates secure JWT token.
     */
    public String generateToken(Authentication authentication, Long userId, String firstName, String lastName) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        
        boolean hasAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean hasTech = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TECHNICIAN"));
        String role = hasAdmin ? "ROLE_ADMIN" : (hasTech ? "ROLE_TECHNICIAN" : "ROLE_USER");

        long now = System.currentTimeMillis();
        long exp = now + jwtExpiration;

        String header = base64UrlEncode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64UrlEncode(
                "{\"sub\":\"" + escapeJson(email) + "\","
                        + "\"role\":\"" + escapeJson(role) + "\","
                        + "\"userId\":" + userId + ","
                        + "\"firstName\":\"" + escapeJson(firstName) + "\","
                        + "\"lastName\":\"" + escapeJson(lastName) + "\","
                        + "\"iat\":" + (now / 1000) + ","
                        + "\"exp\":" + (exp / 1000) + "}"
        );

        String signature = sign(header + "." + payload);
        return header + "." + payload + "." + signature;
    }

    /**
     * Extracts username from token.
     */
    public String getUsernameFromToken(String token) {
        String payload = extractPayload(token);
        return extractJsonString(payload, "sub");
    }

    /**
     * Extracts role from token.
     */
    public String getRoleFromToken(String token) {
        String payload = extractPayload(token);
        return extractJsonString(payload, "role");
    }

    /**
     * Extracts user identifier token.
     */
    public Long getUserIdFromToken(String token) {
        String payload = extractPayload(token);
        return extractJsonLong(payload, "userId");
    }

    /**
     * Validates JWT token signature.
     */
    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;

            String expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!expectedSignature.equals(parts[2])) return false;

            String payload = extractPayload(token);
            long exp = extractJsonLong(payload, "exp");
            return (exp * 1000) > System.currentTimeMillis();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Decodes JWT payload string.
     */
    private String extractPayload(String token) {
        String[] parts = token.split("\\.");
        return new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
    }

    /**
     * Signs string using HMAC.
     */
    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    jwtSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error signing JWT", e);
        }
    }

    /**
     * Encodes string to base64url.
     */
    private String base64UrlEncode(String data) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Escapes JSON special characters.
     */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Extracts JSON string value.
     */
    private String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    /**
     * Extracts JSON long value.
     */
    private Long extractJsonLong(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        return Long.parseLong(json.substring(start, end));
    }
}
