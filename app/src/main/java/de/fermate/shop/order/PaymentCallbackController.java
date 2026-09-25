package de.fermate.shop.order;

import de.fermate.shop.order.PaymentClient.PaymentStatus;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** Server-zu-Server-Callback des Zahlungsdienstleisters. */
@RestController
public class PaymentCallbackController {

  public static final String TOKEN_HEADER = "X-Callback-Token";

  public record Callback(String reference, PaymentStatus status) {}

  private final OrderService orders;
  private final PaymentProperties properties;

  public PaymentCallbackController(OrderService orders, PaymentProperties properties) {
    this.orders = orders;
    this.properties = properties;
  }

  @PostMapping("/api/payments/callback")
  public ResponseEntity<String> callback(
      @RequestHeader(name = TOKEN_HEADER, required = false) String token,
      @RequestBody Callback callback) {
    if (!tokenMatches(token)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ungültiges Callback-Token");
    }
    var result = orders.handlePaymentCallback(callback.reference(), callback.status());
    return switch (result) {
      case UNKNOWN_REFERENCE -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.name());
      default -> ResponseEntity.ok(result.name());
    };
  }

  private boolean tokenMatches(String token) {
    if (token == null || properties.callbackToken() == null) {
      return false;
    }
    return MessageDigest.isEqual(
        token.getBytes(StandardCharsets.UTF_8),
        properties.callbackToken().getBytes(StandardCharsets.UTF_8));
  }
}
