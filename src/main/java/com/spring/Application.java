// Defines root base package namespace for the CampusDESK application
package com.spring;

// Imports SpringApplication bootstrap launcher class for starting Spring applications
import org.springframework.boot.SpringApplication;
// Imports SpringBootApplication annotation bundling configuration, auto-configuration, and scanning
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Marks this class as primary Spring Boot configuration and bootstrap component
@SpringBootApplication
// Declares main entry point class responsible for launching CampusDESK backend
public class Application {

    /**
     * Starts the Spring application.
     */
    // Standard Java application entry point method accepting command-line arguments
	public static void main(String[] args) {
        // Bootstraps Spring application context, starts Tomcat, and initializes beans
		SpringApplication.run(Application.class, args);
    // Closes main entry method
	}

// Closes Application main class definition
}
