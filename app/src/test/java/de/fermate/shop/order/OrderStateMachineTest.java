package de.fermate.shop.order;

import static de.fermate.shop.order.OrderStatus.CANCELLED;
import static de.fermate.shop.order.OrderStatus.NEW;
import static de.fermate.shop.order.OrderStatus.PAID;
import static de.fermate.shop.order.OrderStatus.REFUNDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OrderStateMachineTest {

  @ParameterizedTest(name = "ORD-U-01: {0} → {1} ist erlaubt")
  @CsvSource({
    "NEW, PAID",
    "NEW, CANCELLED",
    "PAID, SHIPPED",
    "PAID, REFUNDED",
    "SHIPPED, DELIVERED",
    "DELIVERED, REFUNDED"
  })
  void allowsHappyPathTransitions(OrderStatus from, OrderStatus to) {
    assertThat(OrderStateMachine.transition(from, to)).isEqualTo(to);
  }

  @ParameterizedTest(name = "ORD-U-02: {0} → {1} ist verboten")
  @CsvSource({
    "PAID, PAID", // Doppelbuchung
    "NEW, SHIPPED", // Versand ohne Zahlung
    "CANCELLED, PAID", // Wiederbelebung einer stornierten Bestellung
    "REFUNDED, SHIPPED",
    "SHIPPED, NEW"
  })
  void rejectsIllegalTransitions(OrderStatus from, OrderStatus to) {
    assertThat(OrderStateMachine.canTransition(from, to)).isFalse();
    assertThatThrownBy(() -> OrderStateMachine.transition(from, to))
        .isInstanceOf(OrderStateMachine.IllegalStateTransitionException.class)
        .hasMessageContaining(from.name())
        .hasMessageContaining(to.name());
  }

  @Test
  @DisplayName("ORD-U-02: Endzustände haben keine Nachfolger")
  void terminalStatesHaveNoSuccessors() {
    for (OrderStatus to : OrderStatus.values()) {
      assertThat(OrderStateMachine.canTransition(CANCELLED, to)).isFalse();
      assertThat(OrderStateMachine.canTransition(REFUNDED, to)).isFalse();
    }
  }

  @Test
  @DisplayName("ORD-U-03: die Bestellung selbst lässt Übergänge nur über die Zustandsmaschine zu")
  void orderDelegatesToStateMachine() {
    var order = new CustomerOrder("c1", "Anna", ShippingMethod.PARCEL, PaymentMethod.CARD);
    assertThat(order.getStatus()).isEqualTo(NEW);

    order.transitionTo(PAID);

    assertThat(order.getStatus()).isEqualTo(PAID);
    assertThat(order.getPaidAt()).isNotNull();
    assertThatThrownBy(() -> order.transitionTo(PAID))
        .isInstanceOf(OrderStateMachine.IllegalStateTransitionException.class);
  }
}
