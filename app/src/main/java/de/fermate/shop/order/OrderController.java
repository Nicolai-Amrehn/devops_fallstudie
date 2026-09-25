package de.fermate.shop.order;

import de.fermate.shop.auth.CustomerIdentity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class OrderController {

  private final OrderService orders;

  public OrderController(OrderService orders) {
    this.orders = orders;
  }

  @GetMapping("/orders")
  public String list(@AuthenticationPrincipal OidcUser user, Model model) {
    model.addAttribute("orders", orders.ordersOf(CustomerIdentity.from(user).customerId()));
    return "orders";
  }

  @GetMapping("/orders/{id}")
  public String show(@AuthenticationPrincipal OidcUser user, @PathVariable long id, Model model) {
    CustomerOrder order =
        orders
            .orderOf(CustomerIdentity.from(user).customerId(), id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bestellung"));
    model.addAttribute("order", order);
    return "order";
  }
}
