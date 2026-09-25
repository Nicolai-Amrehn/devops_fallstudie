package de.fermate.shop.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class VariantStockTest {

  private static Variant variant(String sku, int stock) {
    var v = new Variant() {};
    ReflectionTestUtils.setField(v, "sku", sku);
    ReflectionTestUtils.setField(v, "stock", stock);
    return v;
  }

  @Test
  @DisplayName("CAT-U-04: Bestand 0 heißt ausverkauft, Entnahme über Bestand wird abgelehnt")
  void takeRespectsStock() {
    var v = variant("GIT-1", 2);

    v.take(2);

    assertThat(v.getStock()).isZero();
    assertThat(v.isAvailable()).isFalse();
    assertThatThrownBy(() -> v.take(1)).isInstanceOf(OutOfStockException.class);
  }

  @Test
  @DisplayName("CAT-U-04: Entnahme und Rückbuchung sind symmetrisch")
  void restoreReverts() {
    var v = variant("GIT-1", 3);
    v.take(3);
    v.restore(3);
    assertThat(v.getStock()).isEqualTo(3);
  }

  @Test
  @DisplayName("CAT-U-04: Menge 0 oder negativ ist kein gültiger Kauf")
  void rejectsNonPositiveQuantity() {
    assertThatThrownBy(() -> variant("X", 5).take(0)).isInstanceOf(IllegalArgumentException.class);
  }
}
