package de.fermate.shop.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

  @Query(
      """
      select p from Product p
      where lower(p.name) like lower(concat('%', :q, '%'))
         or lower(p.brand) like lower(concat('%', :q, '%'))
         or lower(p.category) like lower(concat('%', :q, '%'))
      order by p.name
      """)
  List<Product> search(@Param("q") String query);

  List<Product> findAllByOrderByName();
}
