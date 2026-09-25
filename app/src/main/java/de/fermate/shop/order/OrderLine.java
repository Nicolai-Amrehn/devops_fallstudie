package de.fermate.shop.order;

import de.fermate.shop.catalog.PriceCalculator;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "order_line")
public class OrderLine {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String sku;
  private String description;
  private int quantity;
  private BigDecimal unitPrice;
  private boolean bulky;

  protected OrderLine() {}

  public OrderLine(
      String sku, String description, int quantity, BigDecimal unitPrice, boolean bulky) {
    this.sku = sku;
    this.description = description;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
    this.bulky = bulky;
  }

  public BigDecimal getLineTotal() {
    return PriceCalculator.lineTotal(unitPrice, quantity);
  }

  public String getSku() {
    return sku;
  }

  public String getDescription() {
    return description;
  }

  public int getQuantity() {
    return quantity;
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  public boolean isBulky() {
    return bulky;
  }
}
