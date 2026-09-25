package de.fermate.shop.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PriceCalculatorTest {

  @ParameterizedTest(name = "CAT-U-01: netto {0} mit {1} % Aktion → brutto {2}")
  @CsvSource({
    "1680.67, 0, 2000.00", // 1680,67 × 1,19 = 1999,9973 → 2000,00
    "8.39, 0, 9.98", // 8,39 × 1,19 = 9,9841 → 9,98
    "2.09, 0, 2.49", // 2,09 × 1,19 = 2,4871 → 2,49
    "755.46, 10, 809.10", // 679,914 × 1,19 = 809,09766 → 809,10
    "377.31, 15, 381.65", // 320,7135 × 1,19 = 381,649065 → 381,65
    "0.00, 0, 0.00"
  })
  void computesGrossPrice(String net, int promotion, String expected) {
    assertThat(PriceCalculator.grossPrice(new BigDecimal(net), promotion))
        .isEqualByComparingTo(new BigDecimal(expected));
  }

  @Test
  @DisplayName("CAT-U-02: gerundet wird genau einmal am Ende, nicht nach dem Rabatt")
  void roundsOnceAtTheEnd() {
    // 10,05 × 0,9 = 9,045 → ×1,19 = 10,76355 → 10,76.
    // Zwischenrundung auf 9,05 ergäbe 10,7695 → 10,77 – ein Cent zu viel.
    assertThat(PriceCalculator.grossPrice(new BigDecimal("10.05"), 10))
        .isEqualByComparingTo(new BigDecimal("10.76"));
  }

  @Test
  @DisplayName("CAT-U-03: Rabatt außerhalb 0–70 % und negative Preise werden abgelehnt")
  void rejectsInvalidInput() {
    assertThatThrownBy(() -> PriceCalculator.grossPrice(BigDecimal.TEN, 71))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> PriceCalculator.grossPrice(BigDecimal.TEN, -1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> PriceCalculator.grossPrice(new BigDecimal("-1"), 0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("CAT-U-03: Positionssumme = Einzelpreis × Menge")
  void lineTotal() {
    assertThat(PriceCalculator.lineTotal(new BigDecimal("9.98"), 3))
        .isEqualByComparingTo(new BigDecimal("29.94"));
    assertThatThrownBy(() -> PriceCalculator.lineTotal(BigDecimal.ONE, 0))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
