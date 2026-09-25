package de.fermate.shop.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_order")
public class CustomerOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String customerId;
  private String customerName;

  @Enumerated(EnumType.STRING)
  private OrderStatus status = OrderStatus.NEW;

  @Enumerated(EnumType.STRING)
  private ShippingMethod shippingMethod;

  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  private String paymentReference;
  private BigDecimal total;
  private Instant createdAt = Instant.now();
  private Instant paidAt;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private List<OrderLine> lines = new ArrayList<>();

  protected CustomerOrder() {}

  public CustomerOrder(
      String customerId,
      String customerName,
      ShippingMethod shippingMethod,
      PaymentMethod paymentMethod) {
    this.customerId = customerId;
    this.customerName = customerName;
    this.shippingMethod = shippingMethod;
    this.paymentMethod = paymentMethod;
  }

  public void addLine(OrderLine line) {
    lines.add(line);
    total = lines.stream().map(OrderLine::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /** Zustandswechsel nur über die Zustandsmaschine. */
  public void transitionTo(OrderStatus next) {
    this.status = OrderStateMachine.transition(this.status, next);
    if (next == OrderStatus.PAID) {
      this.paidAt = Instant.now();
    }
  }

  public Long getId() {
    return id;
  }

  public String getCustomerId() {
    return customerId;
  }

  public String getCustomerName() {
    return customerName;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public ShippingMethod getShippingMethod() {
    return shippingMethod;
  }

  public PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  public String getPaymentReference() {
    return paymentReference;
  }

  public void setPaymentReference(String paymentReference) {
    this.paymentReference = paymentReference;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getPaidAt() {
    return paidAt;
  }

  public List<OrderLine> getLines() {
    return lines;
  }
}
