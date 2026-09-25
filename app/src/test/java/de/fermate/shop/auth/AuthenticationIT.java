package de.fermate.shop.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.fermate.shop.support.AbstractIntegrationTest;
import de.fermate.shop.support.TestData;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

@AutoConfigureMockMvc
class AuthenticationIT extends AbstractIntegrationTest {

  @Autowired MockMvc mvc;

  @Test
  @DisplayName("AUTH-I-01: geschützte Seite leitet Unangemeldete zum Identity Provider")
  void protectedPageRedirectsToIdentityProvider() throws Exception {
    mvc.perform(get("/checkout"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("http://localhost/oauth2/authorization/keycloak"));

    // Der zweite Sprung zeigt, dass die OIDC-Discovery gegen den Container funktioniert hat:
    // Ziel ist der Autorisierungsendpunkt des importierten Realms.
    String location =
        mvc.perform(get("/oauth2/authorization/keycloak"))
            .andExpect(status().is3xxRedirection())
            .andReturn()
            .getResponse()
            .getRedirectedUrl();
    assertThat(location)
        .startsWith(KEYCLOAK.getAuthServerUrl() + "/realms/fermate/protocol/openid-connect/auth?")
        .contains("client_id=shop")
        .contains("scope=openid");
  }

  @Test
  @DisplayName("AUTH-I-02: Katalog und Warenkorb sind ohne Anmeldung erreichbar")
  void publicPagesAreOpen() throws Exception {
    mvc.perform(get("/")).andExpect(status().isOk());
    mvc.perform(get("/cart")).andExpect(status().isOk());
    mvc.perform(get("/actuator/health")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("AUTH-I-03: angemeldete Kundin erreicht Checkout und Bestellungen")
  void authenticatedCustomerReachesCheckout() throws Exception {
    mvc.perform(get("/checkout").with(TestData.anna())).andExpect(status().isOk());
    mvc.perform(get("/orders").with(oidcLogin())).andExpect(status().isOk());
  }

  @Test
  @DisplayName("AUTH-I-04: der importierte Realm stellt Token mit Realm-Rollen aus")
  void realmIssuesTokensWithRoles() {
    var form = new LinkedMultiValueMap<String, String>();
    form.add("grant_type", "password");
    form.add("client_id", "shop");
    form.add("client_secret", "shop-secret");
    form.add("username", "anna");
    form.add("password", "anna123");

    @SuppressWarnings("unchecked")
    Map<String, Object> token =
        (Map<String, Object>)
            RestClient.create()
                .post()
                .uri(KEYCLOAK.getAuthServerUrl() + "/realms/fermate/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

    assertThat(token).containsKey("access_token");
    String payload = ((String) token.get("access_token")).split("\\.")[1];
    String claims = new String(java.util.Base64.getUrlDecoder().decode(payload));
    assertThat(claims).contains("\"realm_access\"").contains("\"customer\"");
    assertThat(claims).contains("\"iss\":\"" + KEYCLOAK.getAuthServerUrl() + "/realms/fermate\"");
  }
}
