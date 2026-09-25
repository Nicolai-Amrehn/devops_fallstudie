package de.fermate.shop.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.fermate.shop.auth.CustomerIdentity;
import de.fermate.shop.catalog.CatalogService;
import de.fermate.shop.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class PaymentCallbackIT extends AbstractIntegrationTest {

  private static final CustomerIdentity ANNA = new CustomerIdentity("sub-anna", "Anna Beispiel");

  @Autowired MockMvc mvc;
  @Autowired OrderService orders;
  @Autowired CatalogService catalog;

  private CustomerOrder pendingInstallmentOrder(String sku) {
    var cart = new Cart();
    cart.add(sku, 1);
    return orders.placeOrder(ANNA, cart, ShippingMethod.PARCEL, PaymentMethod.INSTALLMENTS);
  }

  private String callback(String reference, String status) {
    return "{\"reference\":\"%s\",\"status\":\"%s\"}".formatted(reference, status);
  }

  @Test
  @DisplayName(
      "ORD-I-03: doppelter AUTHORIZED-Callback → genau eine Buchung, Bestellung einmal BEZAHLT")
  void duplicateCallbackBooksOnce() throws Exception {
    CustomerOrder order = pendingInstallmentOrder("KEY-SP73-B");
    String ref = order.getPaymentReference();

    mvc.perform(
            post("/api/payments/callback")
                .header(PaymentCallbackController.TOKEN_HEADER, CALLBACK_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(callback(ref, "AUTHORIZED")))
        .andExpect(status().isOk())
        .andExpect(content().string("BOOKED"));

    mvc.perform(
            post("/api/payments/callback")
                .header(PaymentCallbackController.TOKEN_HEADER, CALLBACK_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(callback(ref, "AUTHORIZED")))
        .andExpect(status().isOk())
        .andExpect(content().string("ALREADY_PROCESSED"));

    CustomerOrder reloaded = orders.orderOf(ANNA.customerId(), order.getId()).orElseThrow();
    assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.PAID);
    assertThat(orders.receiptCount(order.getId())).isEqualTo(1);
  }

  @Test
  @DisplayName("ORD-I-03b: DECLINED-Callback storniert und gibt den Bestand zurück")
  void declinedCallbackCancelsAndRestoresStock() throws Exception {
    int stockBefore = catalog.variantBySku("GIT-STRAT-SB-R").getStock();
    CustomerOrder order = pendingInstallmentOrder("GIT-STRAT-SB-R");
    assertThat(catalog.variantBySku("GIT-STRAT-SB-R").getStock()).isEqualTo(stockBefore - 1);

    mvc.perform(
            post("/api/payments/callback")
                .header(PaymentCallbackController.TOKEN_HEADER, CALLBACK_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(callback(order.getPaymentReference(), "DECLINED")))
        .andExpect(status().isOk())
        .andExpect(content().string("CANCELLED"));

    assertThat(orders.orderOf(ANNA.customerId(), order.getId()).orElseThrow().getStatus())
        .isEqualTo(OrderStatus.CANCELLED);
    assertThat(catalog.variantBySku("GIT-STRAT-SB-R").getStock()).isEqualTo(stockBefore);
  }

  @Test
  @DisplayName(
      "ORD-I-03c: Callback ohne gültiges Token wird abgewiesen, unbekannte Referenz liefert 404")
  void rejectsBadTokenAndUnknownReference() throws Exception {
    mvc.perform(
            post("/api/payments/callback")
                .header(PaymentCallbackController.TOKEN_HEADER, "falsch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(callback("egal", "AUTHORIZED")))
        .andExpect(status().isUnauthorized());

    mvc.perform(
            post("/api/payments/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(callback("egal", "AUTHORIZED")))
        .andExpect(status().isUnauthorized());

    mvc.perform(
            post("/api/payments/callback")
                .header(PaymentCallbackController.TOKEN_HEADER, CALLBACK_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(callback("gibt-es-nicht", "AUTHORIZED")))
        .andExpect(status().isNotFound());
  }
}
