// Defines the package namespace containing JWT security components
package com.spring.security.jwt;

// Imports Mac class providing message authentication code cryptographic algorithms
import javax.crypto.Mac;
// Imports SecretKeySpec constructing secret cryptographic keys from byte arrays
import javax.crypto.spec.SecretKeySpec;
// Imports StandardCharsets providing standard UTF-8 charset reference
import java.nio.charset.StandardCharsets;
// Imports Base64 utility class for encoding and decoding binary data
import java.util.Base64;
// Imports Date class representing specific instants in time
import java.util.Date;

// Imports Value annotation for injecting externalized configuration property values
import org.springframework.beans.factory.annotation.Value;
// Imports Spring Security Authentication representing the authenticated user principal
import org.springframework.security.core.Authentication;
// Imports GrantedAuthority interface representing security authorities and roles
import org.springframework.security.core.GrantedAuthority;
// Imports UserDetails interface representing core user security credentials
import org.springframework.security.core.userdetails.UserDetails;
// Imports Spring Component annotation declaring class as managed Spring bean
import org.springframework.stereotype.Component;

// Marks this class as Spring component bean managing JSON Web Tokens
@Component
// Declares JwtTokenProvider generating, signing, parsing, and validating JWT bearer tokens
public class JwtTokenProvider {

    // Injects secret cryptographic signing key configured in application properties
    @Value("${jwt.secret}")
    // Stores secret key string utilized for signing and verifying tokens
    private String jwtSecret;

    // Injects token expiration lifespan duration in milliseconds from configuration
    @Value("${jwt.expiration}")
    // Stores validity duration determining expiration time of minted tokens
    private long jwtExpiration;

    // Defines standard HMAC-SHA256 cryptographic algorithm name constant
    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Generates secure JWT token.
     */
    // Builds and cryptographically signs a compact URL-safe JWT access token
    public String generateToken(Authentication authentication, Long userId, String firstName, String lastName) {
        // Casts authenticated principal object into Spring Security UserDetails instance
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // Extracts authenticated user username email address from UserDetails
        String email = userDetails.getUsername();
        
        // Checks whether user authorities contain administrator role authority
        boolean hasAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        // Checks whether user authorities contain technician role authority
        boolean hasTech = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TECHNICIAN"));
        // Resolves authorization role string giving precedence to ADMIN then TECHNICIAN then USER
        String role = hasAdmin ? "ROLE_ADMIN" : (hasTech ? "ROLE_TECHNICIAN" : "ROLE_USER");

        // Captures current epoch timestamp in milliseconds for issuance time
        long now = System.currentTimeMillis();
        // Computes future expiration epoch timestamp adding expiration duration
        long exp = now + jwtExpiration;

        // Constructs and base64url-encodes the standard JWT header JSON specifying HS256 algorithm
        String header = base64UrlEncode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        // Constructs and base64url-encodes JWT payload JSON embedding email, role, userId, and timestamps
        String payload = base64UrlEncode(
                "{\"sub\":\"" + escapeJson(email) + "\","
                        + "\"role\":\"" + escapeJson(role) + "\","
                        + "\"userId\":" + userId + ","
                        + "\"firstName\":\"" + escapeJson(firstName) + "\","
                        + "\"lastName\":\"" + escapeJson(lastName) + "\","
                        + "\"iat\":" + (now / 1000) + ","
                        + "\"exp\":" + (exp / 1000) + "}"
        );

        // Computes HMAC-SHA256 cryptographic signature over combined header and payload
        String signature = sign(header + "." + payload);
        // Combines header, payload, and signature separated by periods returning compact token
        return header + "." + payload + "." + signature;
    // Closes generateToken method
    }

    /**
     * Extracts username from token.
     */
    // Decodes token payload and extracts user email from subject claim
    public String getUsernameFromToken(String token) {
        // Decodes base64url payload segment from compact token string
        String payload = extractPayload(token);
        // Parses subject attribute value corresponding to user email
        return extractJsonString(payload, "sub");
    // Closes getUsernameFromToken method
    }

    /**
     * Extracts role from token.
     */
    // Decodes token payload and extracts assigned role authority claim
    public String getRoleFromToken(String token) {
        // Decodes base64url payload segment from compact token string
        String payload = extractPayload(token);
        // Parses role attribute string from token payload JSON
        return extractJsonString(payload, "role");
    // Closes getRoleFromToken method
    }

    /**
     * Extracts user identifier token.
     */
    // Decodes token payload and extracts user numeric ID claim
    public Long getUserIdFromToken(String token) {
        // Decodes base64url payload segment from compact token string
        String payload = extractPayload(token);
        // Parses userId attribute numeric value from token payload JSON
        return extractJsonLong(payload, "userId");
    // Closes getUserIdFromToken method
    }

    /**
     * Validates JWT token signature.
     */
    // Validates token structure, signature authenticity, and expiration timestamp validity
    public boolean validateToken(String token) {
        // Encloses validation logic in try-catch returning false upon any format anomaly
        try {
            // Splits compact JWT string into three period-delimited segments
            String[] parts = token.split("\\.");
            // Rejects token if it does not contain exactly three parts
            if (parts.length != 3) return false;

            // Re-computes expected HMAC signature using local secret key
            String expectedSignature = sign(parts[0] + "." + parts[1]);
            // Rejects token if signature does not match expected signature
            if (!expectedSignature.equals(parts[2])) return false;

            // Decodes payload to inspect embedded token expiration timestamp
            String payload = extractPayload(token);
            // Parses exp expiration claim value from token payload JSON
            long exp = extractJsonLong(payload, "exp");
            // Verifies that expiration timestamp is in the future compared to now
            return (exp * 1000) > System.currentTimeMillis();
        // Catches any parsing or cryptographic exception during validation
        } catch (Exception e) {
            // Returns false indicating token is invalid, tampered, or expired
            return false;
        // Closes try-catch block
        }
    // Closes validateToken method
    }

    /**
     * Decodes JWT payload string.
     */
    // Private helper method decoding base64url payload segment to UTF-8 JSON
    private String extractPayload(String token) {
        // Splits token into header, payload, and signature components
        String[] parts = token.split("\\.");
        // Decodes base64url payload bytes and converts to UTF-8 string
        return new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
    // Closes extractPayload helper method
    }

    /**
     * Signs string using HMAC.
     */
    // Computes HMAC-SHA256 signature for data string using configured secret key
    private String sign(String data) {
        // Encloses cryptographic signing execution in try-catch block
        try {
            // Obtains Mac algorithm instance for HMAC-SHA256 signing
            Mac mac = Mac.getInstance(HMAC_SHA256);
            // Builds SecretKeySpec wrapping UTF-8 secret key bytes
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    jwtSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            // Initializes Mac cipher instance with secret key specification
            mac.init(secretKeySpec);
            // Computes raw signature hash bytes over UTF-8 input data
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            // Encodes signature hash bytes into unpadded base64url string
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        // Catches cryptographic algorithm or key initialization exceptions
        } catch (Exception e) {
            // Wraps and re-throws checked cryptographic exceptions as RuntimeException
            throw new RuntimeException("Error signing JWT", e);
        // Closes try-catch block
        }
    // Closes sign helper method
    }

    /**
     * Encodes string to base64url.
     */
    // Converts input string to UTF-8 bytes and encodes to unpadded base64url
    private String base64UrlEncode(String data) {
        // Encodes raw string bytes to URL-safe base64 format without padding
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(data.getBytes(StandardCharsets.UTF_8));
    // Closes base64UrlEncode helper method
    }

    /**
     * Escapes JSON special characters.
     */
    // Escapes backslashes and double quotes to ensure valid JSON serialization
    private String escapeJson(String value) {
        // Returns empty string if supplied input value is null
        if (value == null) return "";
        // Replaces backslashes and quotes with escaped representations
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    // Closes escapeJson helper method
    }

    /**
     * Extracts JSON string value.
     */
    // Simple manual parser extracting string property value from JSON string
    private String extractJsonString(String json, String key) {
        // Constructs search prefix matching quoted JSON property key and opening quote
        String search = "\"" + key + "\":\"";
        // Locates starting index of search prefix in JSON string
        int start = json.indexOf(search);
        // Returns null if specified key was not found in JSON
        if (start == -1) return null;
        // Advances start index past the length of the search prefix
        start += search.length();
        // Locates closing quote terminating the string value
        int end = json.indexOf("\"", start);
        // Substrings and returns value between opening and closing quotes
        return json.substring(start, end);
    // Closes extractJsonString helper method
    }

    /**
     * Extracts JSON long value.
     */
    // Simple manual parser extracting numeric long property value from JSON string
    private Long extractJsonLong(String json, String key) {
        // Constructs search prefix matching quoted JSON property key and colon
        String search = "\"" + key + "\":";
        // Locates starting index of search prefix in JSON string
        int start = json.indexOf(search);
        // Returns null if specified key was not found in JSON
        if (start == -1) return null;
        // Advances start index past the length of the search prefix
        start += search.length();
        // Sets end index to begin scanning numeric digits
        int end = start;
        // Advances end index while scanning numeric digits or negative sign
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            // Increments end index scanner position
            end++;
        // Closes while scanning loop
        }
        // Parses scanned digits substring into Java Long value
        return Long.parseLong(json.substring(start, end));
    // Closes extractJsonLong helper method
    }
// Closes JwtTokenProvider class definition
}
