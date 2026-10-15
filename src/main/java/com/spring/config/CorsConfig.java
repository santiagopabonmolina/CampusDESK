// Defines the package namespace containing application configuration beans
package com.spring.config;

// Imports Value annotation for injecting externalized property strings with fallbacks
import org.springframework.beans.factory.annotation.Value;
// Imports Spring Bean annotation declaring factory methods producing managed beans
import org.springframework.context.annotation.Bean;
// Imports Spring Configuration annotation marking class as bean definition source
import org.springframework.context.annotation.Configuration;
// Imports CorsConfiguration defining CORS policies including origins, methods, and headers
import org.springframework.web.cors.CorsConfiguration;
// Imports CorsConfigurationSource interface providing CORS configuration based on requests
import org.springframework.web.cors.CorsConfigurationSource;
// Imports UrlBasedCorsConfigurationSource resolving CORS configurations based on URL path patterns
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// Imports CorsRegistry enabling programmatic registration of global CORS rules
import org.springframework.web.servlet.config.annotation.CorsRegistry;
// Imports WebMvcConfigurer interface providing callback customization methods for Spring MVC
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Imports Arrays utility class for manipulating and creating array collections
import java.util.Arrays;
// Imports Java utility List collection interface for handling sequential lists
import java.util.List;

// Marks this class as Spring configuration component defining CORS settings
@Configuration
// Declares CorsConfig implementing WebMvcConfigurer for Cross-Origin Resource Sharing
public class CorsConfig implements WebMvcConfigurer {

    // Injects allowed origins comma-separated property with default local development ports
    @Value("${cors.allowed-origins:http://127.0.0.1:5500,http://localhost:5500,http://127.0.0.1:5501,http://localhost:5501,http://localhost:63342,http://127.0.0.1:63342,http://localhost:8050,http://127.0.0.1:8050,http://localhost:8080,http://127.0.0.1:8080}")
    // Stores the comma-separated string containing allowed origin URLs
    private String allowedOrigins;

    /**
     * Parses configured CORS origins.
     */
    // Private helper parsing comma-separated origins string into clean string list
    private List<String> getAllowedOriginsList() {
        // Splits allowed origins by comma, trims whitespace, filters empties and returns list
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty() && !s.equals("*"))
                .toList();
    // Closes getAllowedOriginsList helper method
    }

    /**
     * Configures web CORS mappings.
     */
    // Overrides WebMvcConfigurer method to configure global CORS endpoint mappings
    @Override
    // Configures CORS mappings for Spring MVC endpoints enabling cross-origin requests
    public void addCorsMappings(CorsRegistry registry) {
        // Retrieves parsed list of allowed origin host addresses
        List<String> origins = getAllowedOriginsList();
        // Registers path pattern permitting origins, methods, headers, and credentials
        registry.addMapping("/**")
                .allowedOrigins(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    // Closes addCorsMappings method
    }

    /**
     * Provides CORS configuration source.
     */
    // Registers CorsConfigurationSource bean for Spring Security integration
    @Bean
    // Creates and configures UrlBasedCorsConfigurationSource with credentials and headers
    public CorsConfigurationSource corsConfigurationSource() {
        // Instantiates CorsConfiguration container for allowed origins, methods, and headers
        CorsConfiguration configuration = new CorsConfiguration();
        // Retrieves parsed list of allowed origin host addresses
        List<String> origins = getAllowedOriginsList();

        // Sets permitted client origin addresses on CORS configuration
        configuration.setAllowedOrigins(origins);
        // Sets permitted HTTP methods on CORS configuration
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // Sets permitted HTTP request headers on CORS configuration
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        // Enables credentials support allowing cookies and authorization headers
        configuration.setAllowCredentials(true);
        // Sets preflight response caching duration to one hour
        configuration.setMaxAge(3600L);

        // Instantiates URL-based CORS configuration source mapping paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Registers configured CORS rules across all application URL routes
        source.registerCorsConfiguration("/**", configuration);
        // Returns the configured CorsConfigurationSource bean to Spring container
        return source;
    // Closes corsConfigurationSource bean method
    }
// Closes CorsConfig class definition
}
