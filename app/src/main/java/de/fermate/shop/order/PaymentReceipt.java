package de.fermate.shop.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Eine Buchung je Zahlungsreferenz. Die Unique-Constraint verhindert doppelte Buchungen auch dann,
 * wenn zwei Callbacks gleichzeitig die Zustandsprüfung passieren.
 */
@Entity
@Table(name = "payment_receipt")
public class PaymentReceipt {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String paymentReference;

  private Long orderId;
  private BigDecimal amount;
  private Instant receivedAt = Instant.now();

  protected PaymentReceipt() {}

  public PaymentReceipt(String paymentReference, Long orderId, BigDecimal amount) {
    this.paymentReference = paymentReference;
    this.orderId = orderId;
    this.amount = amount;
  }

  public String getPaymentReference() {
    return paymentReference;
  }

  public Long getOrderId() {
    return orderId;
  }

  public BigDecimal getAmount() {
    return amount;
  }
}
