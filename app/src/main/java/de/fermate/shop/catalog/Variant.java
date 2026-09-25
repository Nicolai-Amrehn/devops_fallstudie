package de.fermate.shop.catalog;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Bestellbare Ausprägung eines Artikels, z. B. Farbe oder Linkshänderausführung, mit eigenem
 * Bestand.
 */
@Entity
@Table(name = "variant")
public class Variant {

  @Id private Long id;
  private String sku;
  private String color;
  private String handedness;
  private int stock;

  @ManyToOne(optional = false)
  @JoinColumn(name = "product_id")
  private Product product;

  protected Variant() {}

  public Long getId() {
    return id;
  }

  public String getSku() {
    return sku;
  }

  public String getColor() {
    return color;
  }

  public String getHandedness() {
    return handedness;
  }

  public int getStock() {
    return stock;
  }

  public Product getProduct() {
    return product;
  }

  public boolean isAvailable() {
    return stock > 0;
  }

  public String getLabel() {
    StringBuilder label = new StringBuilder();
    if (color != null) {
      label.append(color);
    }
    if (handedness != null) {
      label.append(label.isEmpty() ? "" : ", ").append(handedness);
    }
    return label.isEmpty() ? "Standard" : label.toString();
  }

  /** Reduziert den Bestand; wirft, wenn nicht genug vorrätig ist. */
  public void take(int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("Menge muss positiv sein");
    }
    if (quantity > stock) {
      throw new OutOfStockException(sku, stock, quantity);
    }
    stock -= quantity;
  }

  public void restore(int quantity) {
    stock += quantity;
  }
}
