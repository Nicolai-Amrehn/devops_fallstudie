package de.fermate.shop.order;

/** Fachlicher Abbruch des Checkouts; die Nachricht ist für den Kunden gedacht. */
public class CheckoutException extends RuntimeException {
  public CheckoutException(String message) {
    super(message);
  }
}
