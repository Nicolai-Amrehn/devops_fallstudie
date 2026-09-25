package de.fermate.shop.catalog;

public class UnknownSkuException extends RuntimeException {
  public UnknownSkuException(String sku) {
    super("Unbekannte Artikelnummer: " + sku);
  }
}
