package de.fermate.shop.order;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Zugang zum Zahlungsdienstleister. Das Callback-Token kommt aus der Umgebung (lokal .env, in CI
 * ein Secret) und steht nirgends im Repository.
 */
@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(String baseUrl, String callbackToken) {}
