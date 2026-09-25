package de.fermate.shop.order;

import java.math.BigDecimal;
import java.time.Duration;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Adapter zur REST-API des Zahlungsdienstleisters (im PoC durch WireMock vertreten). */
@Component
public class PaymentClient {

  public record PaymentRequest(long orderId, BigDecimal amount, PaymentMethod method) {}

  public record PaymentResponse(String reference, PaymentStatus status) {}

  public record CreditCheckRequest(String customerId, BigDecimal amount) {}

  public record CreditCheckResponse(boolean approved, String reason) {}

  public enum PaymentStatus {
    AUTHORIZED,
    PENDING,
    DECLINED
  }

  private final RestClient http;

  public PaymentClient(PaymentProperties properties, RestClient.Builder builder) {
    var settings =
        ClientHttpRequestFactorySettings.DEFAULTS
            .withConnectTimeout(Duration.ofSeconds(2))
            .withReadTimeout(Duration.ofSeconds(3));
    this.http =
        builder
            .baseUrl(properties.baseUrl())
            .requestFactory(ClientHttpRequestFactories.get(settings))
            .build();
  }

  public PaymentResponse createPayment(PaymentRequest request) {
    return http.post().uri("/payments").body(request).retrieve().body(PaymentResponse.class);
  }

  public boolean isCreditApproved(String customerId, BigDecimal amount) {
    CreditCheckResponse response =
        http.post()
            .uri("/credit-checks")
            .body(new CreditCheckRequest(customerId, amount))
            .retrieve()
            .body(CreditCheckResponse.class);
    return response != null && response.approved();
  }
}
