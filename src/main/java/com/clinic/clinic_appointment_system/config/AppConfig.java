package com.clinic.clinic_appointment_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Application-level bean configuration.
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate used by NotificationServiceClient to make HTTP calls
     * to the external (mock) Notification Service.
     *
     * In production this would be tuned with timeouts, retry policies, and
     * a circuit-breaker (e.g. Resilience4j) so that Notification Service
     * unavailability never blocks appointment booking.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
