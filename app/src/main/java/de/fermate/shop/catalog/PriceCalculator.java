package de.fermate.shop.catalog;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Preisberechnung: Nettopreis → Aktionsrabatt → Umsatzsteuer → kaufmännische Rundung auf Cent.
 * Rabatt und Steuer werden auf dem ungerundeten Wert gerechnet; gerundet wird genau einmal.
 */
public final class PriceCalculator {

  static final BigDecimal VAT_RATE = new BigDecimal("0.19");
  static final int MAX_PROMOTION_PERCENT = 70;

  private PriceCalculator() {}

  public static BigDecimal grossPrice(BigDecimal netPrice, int promotionPercent) {
    if (netPrice == null || netPrice.signum() < 0) {
      throw new IllegalArgumentException("Nettopreis muss >= 0 sein");
    }
    if (promotionPercent < 0 || promotionPercent > MAX_PROMOTION_PERCENT) {
      throw new IllegalArgumentException(
          "Aktionsrabatt muss zwischen 0 und " + MAX_PROMOTION_PERCENT + " % liegen");
    }
    BigDecimal factor = BigDecimal.valueOf(100 - promotionPercent).movePointLeft(2); // z. B. 0.85
    BigDecimal discounted = netPrice.multiply(factor);
    BigDecimal gross = discounted.multiply(BigDecimal.ONE.add(VAT_RATE));
    return gross.setScale(2, RoundingMode.HALF_UP);
  }

  public static BigDecimal lineTotal(BigDecimal unitGross, int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("Menge muss positiv sein");
    }
    return unitGross.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
  }
}
