package de.fermate.shop.catalog;

public class OutOfStockException extends RuntimeException {

  public OutOfStockException(String sku, int available, int requested) {
    super("Variante %s: %d angefragt, %d verfügbar".formatted(sku, requested, available));
  }
}
