package de.fermate.shop.order;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CartController {

  private final Cart cart;
  private final OrderService orders;

  public CartController(Cart cart, OrderService orders) {
    this.cart = cart;
    this.orders = orders;
  }

  @PostMapping("/cart/items")
  public String add(@RequestParam String sku, @RequestParam(defaultValue = "1") int quantity) {
    cart.add(sku, quantity);
    return "redirect:/cart";
  }

  @PostMapping("/cart/items/{sku}/remove")
  public String remove(@PathVariable String sku) {
    cart.remove(sku);
    return "redirect:/cart";
  }

  @GetMapping("/cart")
  public String show(Model model) {
    model.addAttribute("cart", orders.view(cart));
    return "cart";
  }
}
