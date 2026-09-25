package de.fermate.shop.order;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/** Warenkorb in der Sitzung: Artikelnummer → Menge. Preise werden erst im Checkout gerechnet. */
@Component
@SessionScope
public class Cart implements Serializable {

  private final Map<String, Integer> items = new LinkedHashMap<>();

  public void add(String sku, int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("Menge muss positiv sein");
    }
    items.merge(sku, quantity, Integer::sum);
  }

  public void remove(String sku) {
    items.remove(sku);
  }

  public void clear() {
    items.clear();
  }

  public boolean isEmpty() {
    return items.isEmpty();
  }

  public Map<String, Integer> getItems() {
    return Collections.unmodifiableMap(items);
  }

  public int getTotalQuantity() {
    return items.values().stream().mapToInt(Integer::intValue).sum();
  }
}
