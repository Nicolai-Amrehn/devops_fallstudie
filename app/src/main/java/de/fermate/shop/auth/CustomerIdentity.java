package de.fermate.shop.auth;

import java.util.Map;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Die Identität, unter der Bestellungen gespeichert werden: das stabile {@code sub} des Identity
 * Providers, nie der änderbare Benutzername. Der Anzeigename fällt gestuft zurück.
 */
public record CustomerIdentity(String customerId, String displayName) {

  public static CustomerIdentity from(OidcUser user) {
    return from(user.getClaims());
  }

  public static CustomerIdentity from(Map<String, Object> claims) {
    Object sub = claims.get("sub");
    if (!(sub instanceof String id) || id.isBlank()) {
      throw new IllegalArgumentException("ID-Token ohne sub-Claim");
    }
    String name = firstNonBlank(claims, "name", "preferred_username", "email");
    return new CustomerIdentity(id, name == null ? id : name);
  }

  private static String firstNonBlank(Map<String, Object> claims, String... keys) {
    for (String key : keys) {
      if (claims.get(key) instanceof String value && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }
}
