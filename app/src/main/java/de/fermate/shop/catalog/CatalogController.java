package de.fermate.shop.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class CatalogController {

  private final CatalogService catalog;

  public CatalogController(CatalogService catalog) {
    this.catalog = catalog;
  }

  @GetMapping("/")
  public String index(@RequestParam(name = "q", required = false) String query, Model model) {
    model.addAttribute("query", query == null ? "" : query);
    model.addAttribute("products", catalog.search(query));
    return "index";
  }

  @GetMapping("/products/{id}")
  public String product(@PathVariable long id, Model model) {
    Product product =
        catalog
            .product(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artikel"));
    model.addAttribute("product", product);
    return "product";
  }
}
