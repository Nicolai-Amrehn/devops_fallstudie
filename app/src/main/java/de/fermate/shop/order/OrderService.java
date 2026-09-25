package de.fermate.shop.order;

import de.fermate.shop.auth.CustomerIdentity;
import de.fermate.shop.catalog.CatalogService;
import de.fermate.shop.catalog.OutOfStockException;
import de.fermate.shop.catalog.Variant;
import de.fermate.shop.order.PaymentClient.PaymentRequest;
import de.fermate.shop.order.PaymentClient.PaymentResponse;
import de.fermate.shop.order.PaymentClient.PaymentStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

@Service
public class OrderService {

  private static final Logger log = LoggerFactory.getLogger(OrderService.class);

  private final CatalogService catalog;
  private final OrderRepository orders;
  private final PaymentReceiptRepository receipts;
  private final PaymentClient payments;

  public OrderService(
      CatalogService catalog,
      OrderRepository orders,
      PaymentReceiptRepository receipts,
      PaymentClient payments) {
    this.catalog = catalog;
    this.orders = orders;
    this.receipts = receipts;
    this.payments = payments;
  }

  /** Warenkorb bepreisen, ohne zu bestellen (für Warenkorb- und Checkout-Seite). */
  @Transactional(readOnly = true)
  public CartView view(Cart cart) {
    var lines = cart.getItems().entrySet().stream().map(this::toLine).toList();
    return new CartView(lines);
  }

  private OrderLine toLine(Map.Entry<String, Integer> item) {
    Variant variant = catalog.variantBySku(item.getKey());
    var product = variant.getProduct();
    return new OrderLine(
        variant.getSku(),
        product.getName() + " (" + variant.getLabel() + ")",
        item.getValue(),
        product.getGrossPrice(),
        product.isBulky());
  }

  /**
   * Bestellung anlegen: Bestand reservieren, Ratenkauf prüfen, Zahlung anstoßen. Läuft in einer
   * Transaktion – schlägt der Zahlungsdienstleister fehl, bleibt auch der Bestand unangetastet.
   */
  @Transactional
  public CustomerOrder placeOrder(
      CustomerIdentity customer, Cart cart, ShippingMethod shipping, PaymentMethod payment) {
    if (cart.isEmpty()) {
      throw new CheckoutException("Der Warenkorb ist leer.");
    }
    CartView view = view(cart);
    ShippingPolicy.requireAllowed(shipping, view.containsBulkyItem());

    var order = new CustomerOrder(customer.customerId(), customer.displayName(), shipping, payment);
    view.lines().forEach(order::addLine);

    if (payment == PaymentMethod.INSTALLMENTS
        && !payments.isCreditApproved(customer.customerId(), order.getTotal())) {
      throw new CheckoutException(
          "Ratenkauf wurde für diesen Betrag nicht genehmigt. Bitte andere Zahlungsart wählen.");
    }

    try {
      cart.getItems().forEach((sku, qty) -> catalog.variantBySku(sku).take(qty));
    } catch (OutOfStockException e) {
      throw new CheckoutException("Ein Artikel ist nicht mehr in der gewünschten Menge verfügbar.");
    }

    order = orders.save(order);
    PaymentResponse response;
    try {
      response =
          payments.createPayment(new PaymentRequest(order.getId(), order.getTotal(), payment));
    } catch (RestClientException e) {
      log.warn("Zahlungsdienstleister nicht erreichbar für Bestellung {}", order.getId(), e);
      throw new CheckoutException(
          "Der Zahlungsdienstleister ist derzeit nicht erreichbar. Bitte später erneut versuchen.");
    }
    if (response == null || response.status() == PaymentStatus.DECLINED) {
      throw new CheckoutException(
          "Die Zahlung wurde abgelehnt. Es wurde keine Bestellung angelegt.");
    }
    order.setPaymentReference(response.reference());
    if (response.status() == PaymentStatus.AUTHORIZED) {
      book(order, response.reference());
    }
    cart.clear();
    return order;
  }

  /**
   * Callback des Zahlungsdienstleisters. Idempotent: derselbe Callback darf beliebig oft eintreffen
   * und führt zu genau einer Buchung.
   */
  @Transactional
  public CallbackResult handlePaymentCallback(String reference, PaymentStatus status) {
    Optional<CustomerOrder> maybe = orders.findByPaymentReference(reference);
    if (maybe.isEmpty()) {
      return CallbackResult.UNKNOWN_REFERENCE;
    }
    CustomerOrder order = maybe.get();
    return switch (status) {
      case AUTHORIZED -> {
        if (order.getStatus() != OrderStatus.NEW) {
          log.info(
              "Callback für {} ignoriert, Bestellung ist bereits {}", reference, order.getStatus());
          yield CallbackResult.ALREADY_PROCESSED;
        }
        book(order, reference);
        yield CallbackResult.BOOKED;
      }
      case DECLINED -> {
        if (order.getStatus() != OrderStatus.NEW) {
          yield CallbackResult.ALREADY_PROCESSED;
        }
        order.transitionTo(OrderStatus.CANCELLED);
        order.getLines().forEach(l -> catalog.variantBySku(l.getSku()).restore(l.getQuantity()));
        yield CallbackResult.CANCELLED;
      }
      case PENDING -> CallbackResult.ALREADY_PROCESSED;
    };
  }

  private void book(CustomerOrder order, String reference) {
    receipts.save(new PaymentReceipt(reference, order.getId(), order.getTotal()));
    order.transitionTo(OrderStatus.PAID);
  }

  @Transactional(readOnly = true)
  public List<CustomerOrder> ordersOf(String customerId) {
    return orders.findByCustomerIdOrderByCreatedAtDesc(customerId);
  }

  @Transactional(readOnly = true)
  public Optional<CustomerOrder> orderOf(String customerId, long orderId) {
    return orders.findById(orderId).filter(o -> o.getCustomerId().equals(customerId));
  }

  public long receiptCount(long orderId) {
    return receipts.countByOrderId(orderId);
  }

  public enum CallbackResult {
    BOOKED,
    CANCELLED,
    ALREADY_PROCESSED,
    UNKNOWN_REFERENCE
  }

  public record CartView(List<OrderLine> lines) {
    public BigDecimal total() {
      return lines.stream().map(OrderLine::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean containsBulkyItem() {
      return lines.stream().anyMatch(OrderLine::isBulky);
    }

    public boolean isEmpty() {
      return lines.isEmpty();
    }
  }
}
