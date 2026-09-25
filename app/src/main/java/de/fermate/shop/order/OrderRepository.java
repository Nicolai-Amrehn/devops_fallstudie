package de.fermate.shop.order;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
  Optional<CustomerOrder> findByPaymentReference(String paymentReference);

  List<CustomerOrder> findByCustomerIdOrderByCreatedAtDesc(String customerId);
}
