package de.fermate.shop.support;

import java.util.Map;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor;

public final class TestData {

  /** Angemeldete Kundin, wie sie der Identity Provider liefern würde. */
  public static final String ANNA_SUB = "sub-anna-0001";

  private TestData() {}

  public static OidcLoginRequestPostProcessor anna() {
    return SecurityMockMvcRequestPostProcessors.oidcLogin()
        .idToken(
            token ->
                token.claims(
                    c ->
                        c.putAll(
                            Map.of(
                                "sub",
                                ANNA_SUB,
                                "name",
                                "Anna Beispiel",
                                "preferred_username",
                                "anna",
                                "realm_access",
                                Map.of("roles", java.util.List.of("customer"))))));
  }
}
