package de.fermate.shop.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CartTest {

  @Test
  @DisplayName("ORD-U-05: gleiche SKU addiert die Menge statt eine zweite Position anzulegen")
  void mergesSameSku() {
    var cart = new Cart();
    cart.add("CAB-6M", 2);
    cart.add("CAB-6M", 3);
    cart.add("PIK-12", 1);

    assertThat(cart.getItems()).containsEntry("CAB-6M", 5).containsEntry("PIK-12", 1);
    assertThat(cart.getTotalQuantity()).isEqualTo(6);
  }

  @Test
  @DisplayName("ORD-U-05: Menge 0 wird abgelehnt, Entfernen und Leeren funktionieren")
  void rejectsZeroAndClears() {
    var cart = new Cart();
    assertThatThrownBy(() -> cart.add("CAB-6M", 0)).isInstanceOf(IllegalArgumentException.class);
    cart.add("CAB-6M", 1);
    cart.remove("CAB-6M");
    assertThat(cart.isEmpty()).isTrue();
  }
}
