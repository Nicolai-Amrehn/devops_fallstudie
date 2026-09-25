package de.fermate.shop.catalog;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantRepository extends JpaRepository<Variant, Long> {
  Optional<Variant> findBySku(String sku);
}
