package de.fermate.shop.order;

public enum ShippingMethod {
  PARCEL("Paketversand"),
  FREIGHT("Spedition");

  private final String label;

  ShippingMethod(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
