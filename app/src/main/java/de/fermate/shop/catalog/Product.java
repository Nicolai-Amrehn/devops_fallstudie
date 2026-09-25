package de.fermate.shop.catalog;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Ein Artikel des Sortiments; der Bestand hängt an den Varianten. */
@Entity
@Table(name = "product")
public class Product {

  @Id private Long id;
  private String name;
  private String brand;
  private String category;

  /** Nettopreis (ohne Umsatzsteuer, vor Aktionsrabatt). */
  private BigDecimal netPrice;

  /** Aktionsrabatt in Prozent, 0 = keine Aktion. */
  private int promotionPercent;

  /** Sperrgut (Klavier, Schlagzeug) darf nur per Spedition versendet werden. */
  private boolean bulky;

  private boolean bStock;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @OrderBy("sku")
  private List<Variant> variants = new ArrayList<>();

  protected Product() {}

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getBrand() {
    return brand;
  }

  public String getCategory() {
    return category;
  }

  public BigDecimal getNetPrice() {
    return netPrice;
  }

  public int getPromotionPercent() {
    return promotionPercent;
  }

  public boolean isBulky() {
    return bulky;
  }

  public boolean isBStock() {
    return bStock;
  }

  public List<Variant> getVariants() {
    return variants;
  }

  public BigDecimal getGrossPrice() {
    return PriceCalculator.grossPrice(netPrice, promotionPercent);
  }

  public boolean isAvailable() {
    return variants.stream().anyMatch(Variant::isAvailable);
  }
}
