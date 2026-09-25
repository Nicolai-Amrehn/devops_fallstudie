package de.fermate.shop.order;

public enum PaymentMethod {
  CARD("Kreditkarte"),
  INSTALLMENTS("Ratenkauf");

  private final String label;

  PaymentMethod(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
