package de.fermate.shop.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShippingPolicyTest {

  @Test
  @DisplayName("ORD-U-04: ohne Sperrgut sind Paket und Spedition wählbar")
  void withoutBulkyBothMethods() {
    assertThat(ShippingPolicy.allowedMethods(false))
        .containsExactlyInAnyOrder(ShippingMethod.PARCEL, ShippingMethod.FREIGHT);
    assertThatCode(() -> ShippingPolicy.requireAllowed(ShippingMethod.PARCEL, false))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("ORD-U-04: mit Sperrgut ist nur Spedition zulässig")
  void bulkyForcesFreight() {
    assertThat(ShippingPolicy.allowedMethods(true)).containsExactly(ShippingMethod.FREIGHT);
    assertThatThrownBy(() -> ShippingPolicy.requireAllowed(ShippingMethod.PARCEL, true))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
