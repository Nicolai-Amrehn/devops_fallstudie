package de.fermate.shop.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.fermate.shop.support.AbstractIntegrationTest;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class CatalogIT extends AbstractIntegrationTest {

  @Autowired MockMvc mvc;
  @Autowired CatalogService catalog;

  @Test
  @DisplayName(
      "CAT-I-01: Suche findet über Name, Marke und Kategorie, unabhängig von Groß-/Kleinschreibung")
  void searchMatchesNameBrandAndCategory() {
    assertThat(catalog.search("stratos"))
        .extracting(Product::getName)
        .containsExactly("Stratos Classic E-Gitarre");
    assertThat(catalog.search("RIMSHOT")).extracting(Product::getBrand).containsExactly("Rimshot");
    assertThat(catalog.search("Kabel")).extracting(Product::getCategory).containsExactly("Kabel");
    assertThat(catalog.search("Basics")).hasSize(2);
    assertThat(catalog.search("Cembalo")).isEmpty();
    assertThat(catalog.search("  ")).hasSize(6);
  }

  @Test
  @DisplayName("CAT-I-02: ausverkaufte Variante blockiert nicht die Schwestervariante")
  void soldOutVariantDoesNotBlockSiblings() throws Exception {
    Product guitar = catalog.product(1).orElseThrow();

    assertThat(guitar.isAvailable()).isTrue();
    assertThat(catalog.variantBySku("GIT-STRAT-SB-L").isAvailable()).isFalse();
    assertThat(catalog.variantBySku("GIT-STRAT-SB-R").isAvailable()).isTrue();

    String html =
        mvc.perform(get("/products/1"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(row(html, "GIT-STRAT-SB-L"))
        .contains("data-testid=\"sold-out\"")
        .contains("ausverkauft");
    assertThat(row(html, "GIT-STRAT-SB-R"))
        .contains("data-testid=\"add-to-cart\"")
        .contains("verfügbar");
  }

  /**
   * Die Tabellenzeile einer Variante aus dem gerenderten HTML (Thymeleaf liefert HTML5, kein XML).
   */
  private static String row(String html, String sku) {
    var m =
        Pattern.compile("<tr data-testid=\"variant-" + sku + "\">(.*?)</tr>", Pattern.DOTALL)
            .matcher(html);
    assertThat(m.find()).as("Zeile für %s", sku).isTrue();
    return m.group(1);
  }

  @Test
  @DisplayName("CAT-I-03: Bruttopreise aus der Datenbank werden korrekt gerendert")
  void rendersGrossPrices() throws Exception {
    mvc.perform(get("/products/1"))
        .andExpect(status().isOk())
        .andExpect(
            content().string(org.hamcrest.Matchers.containsString("2.000,00 € inkl. MwSt.")));
    mvc.perform(get("/").param("q", "Drum"))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("809,10 €")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("-10 %")));
  }
}
