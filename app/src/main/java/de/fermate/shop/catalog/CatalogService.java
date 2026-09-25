package de.fermate.shop.catalog;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogService {

  private final ProductRepository products;
  private final VariantRepository variants;

  public CatalogService(ProductRepository products, VariantRepository variants) {
    this.products = products;
    this.variants = variants;
  }

  public List<Product> search(String query) {
    if (query == null || query.isBlank()) {
      return products.findAllByOrderByName();
    }
    return products.search(query.trim());
  }

  public Optional<Product> product(long id) {
    return products.findById(id);
  }

  public Variant variantBySku(String sku) {
    return variants.findBySku(sku).orElseThrow(() -> new UnknownSkuException(sku));
  }
}
