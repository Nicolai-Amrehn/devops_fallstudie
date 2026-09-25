package de.fermate.shop.order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentReceiptRepository extends JpaRepository<PaymentReceipt, Long> {
  long countByOrderId(Long orderId);
}
