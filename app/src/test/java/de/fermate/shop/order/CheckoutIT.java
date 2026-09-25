package de.fermate.shop.order;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.fermate.shop.auth.CustomerIdentity;
import de.fermate.shop.catalog.CatalogService;
import de.fermate.shop.support.AbstractIntegrationTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CheckoutIT extends AbstractIntegrationTest {

  private static final CustomerIdentity ANNA = new CustomerIdentity("sub-anna", "Anna Beispiel");

  @Autowired OrderService orders;
  @Autowired CatalogService catalog;

  private Cart cartWith(String sku, int qty) {
    var cart = new Cart();
    cart.add(sku, qty);
    return cart;
  }

  @Test
  @DisplayName(
      "ORD-I-01: Kartenzahlung autorisiert → Bestellung BEZAHLT, Bestand reduziert, eine Buchung")
  void cardPaymentAuthorized() {
    int stockBefore = catalog.variantBySku("CAB-6M").getStock();
    var cart = cartWith("CAB-6M", 3);

    CustomerOrder order = orders.placeOrder(ANNA, cart, ShippingMethod.PARCEL, PaymentMethod.CARD);

    assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("29.94"));
    assertThat(order.getPaymentReference()).startsWith("pay-");
    assertThat(catalog.variantBySku("CAB-6M").getStock()).isEqualTo(stockBefore - 3);
    assertThat(orders.receiptCount(order.getId())).isEqualTo(1);
    assertThat(cart.isEmpty()).isTrue();
    PAYMENT.verify(1, postRequestedFor(urlPathEqualTo("/payments")));
  }

  @Test
  @DisplayName(
      "ORD-I-02: Zahlungsdienstleister antwortet mit HTTP 500 → keine Bestellung, Bestand unverändert")
  void paymentProviderFailureRollsBack() {
    PAYMENT.stubFor(
        post(urlPathEqualTo("/payments")).atPriority(1).willReturn(aResponse().withStatus(500)));
    int stockBefore = catalog.variantBySku("PIK-12").getStock();

    assertThatThrownBy(
            () ->
                orders.placeOrder(
                    ANNA, cartWith("PIK-12", 2), ShippingMethod.PARCEL, PaymentMethod.CARD))
        .isInstanceOf(CheckoutException.class)
        .hasMessageContaining("nicht erreichbar");

    assertThat(catalog.variantBySku("PIK-12").getStock()).isEqualTo(stockBefore);
    assertThat(orders.ordersOf(ANNA.customerId()))
        .noneMatch(o -> o.getLines().stream().anyMatch(l -> l.getSku().equals("PIK-12")));
  }

  @Test
  @DisplayName("ORD-I-02b: Timeout beim Zahlungsdienstleister wird wie ein Ausfall behandelt")
  void paymentProviderTimeoutRollsBack() {
    PAYMENT.stubFor(
        post(urlPathEqualTo("/payments"))
            .atPriority(1)
            .willReturn(aResponse().withStatus(201).withFixedDelay(5000)));
    int stockBefore = catalog.variantBySku("PIK-12").getStock();

    assertThatThrownBy(
            () ->
                orders.placeOrder(
                    ANNA, cartWith("PIK-12", 1), ShippingMethod.PARCEL, PaymentMethod.CARD))
        .isInstanceOf(CheckoutException.class);

    assertThat(catalog.variantBySku("PIK-12").getStock()).isEqualTo(stockBefore);
  }

  @Test
  @DisplayName(
      "ORD-I-04: Ratenkauf über dem Limit wird abgelehnt, bevor Bestand oder Zahlung berührt werden")
  void installmentsRejectedByCreditCheck() {
    int stockBefore = catalog.variantBySku("PNO-F188-BK").getStock();

    assertThatThrownBy(
            () ->
                orders.placeOrder(
                    ANNA,
                    cartWith("PNO-F188-BK", 1),
                    ShippingMethod.FREIGHT,
                    PaymentMethod.INSTALLMENTS))
        .isInstanceOf(CheckoutException.class)
        .hasMessageContaining("Ratenkauf");

    assertThat(catalog.variantBySku("PNO-F188-BK").getStock()).isEqualTo(stockBefore);
    PAYMENT.verify(1, postRequestedFor(urlPathEqualTo("/credit-checks")));
    PAYMENT.verify(0, postRequestedFor(urlPathEqualTo("/payments")));
  }

  @Test
  @DisplayName(
      "ORD-I-05: Ratenkauf im Limit → Zahlung PENDING, Bestellung bleibt NEU bis zum Callback")
  void installmentsWithinLimitStaysNewUntilCallback() {
    CustomerOrder order =
        orders.placeOrder(
            ANNA, cartWith("GIT-STRAT-BK-R", 1), ShippingMethod.PARCEL, PaymentMethod.INSTALLMENTS);

    assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
    assertThat(order.getPaymentReference()).startsWith("inst-");
    assertThat(orders.receiptCount(order.getId())).isZero();
  }

  @Test
  @DisplayName(
      "ORD-I-06: Sperrgut per Paket wird vom Server abgelehnt, auch wenn das Formular umgangen wird")
  void bulkyItemRejectsParcel() {
    assertThatThrownBy(
            () ->
                orders.placeOrder(
                    ANNA, cartWith("DRM-STUDIO-RD", 1), ShippingMethod.PARCEL, PaymentMethod.CARD))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Sperrgut");
  }

  @Test
  @DisplayName("ORD-I-07: Kartenzahlung abgelehnt → keine Bestellung")
  void cardDeclined() {
    assertThatThrownBy(
            () ->
                orders.placeOrder(
                    ANNA, cartWith("PNO-F188-BK", 1), ShippingMethod.FREIGHT, PaymentMethod.CARD))
        .isInstanceOf(CheckoutException.class)
        .hasMessageContaining("abgelehnt");
    assertThat(catalog.variantBySku("PNO-F188-BK").getStock()).isEqualTo(1);
  }
}
