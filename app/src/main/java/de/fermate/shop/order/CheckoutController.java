package de.fermate.shop.order;

import de.fermate.shop.auth.CustomerIdentity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CheckoutController {

  private final Cart cart;
  private final OrderService orders;

  public CheckoutController(Cart cart, OrderService orders) {
    this.cart = cart;
    this.orders = orders;
  }

  @GetMapping("/checkout")
  public String form(Model model) {
    return render(model, null);
  }

  @PostMapping("/checkout")
  public String submit(
      @AuthenticationPrincipal OidcUser user,
      @RequestParam ShippingMethod shippingMethod,
      @RequestParam PaymentMethod paymentMethod,
      Model model) {
    try {
      CustomerOrder order =
          orders.placeOrder(CustomerIdentity.from(user), cart, shippingMethod, paymentMethod);
      return "redirect:/orders/" + order.getId();
    } catch (CheckoutException e) {
      return render(model, e.getMessage());
    }
  }

  private String render(Model model, String error) {
    OrderService.CartView view = orders.view(cart);
    model.addAttribute("cart", view);
    model.addAttribute("shippingMethods", ShippingPolicy.allowedMethods(view.containsBulkyItem()));
    model.addAttribute("paymentMethods", PaymentMethod.values());
    model.addAttribute("error", error);
    return "checkout";
  }
}
