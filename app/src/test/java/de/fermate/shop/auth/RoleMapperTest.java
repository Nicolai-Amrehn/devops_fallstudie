package de.fermate.shop.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class RoleMapperTest {

  private final RoleMapper mapper = new RoleMapper();

  @Test
  @DisplayName("AUTH-U-01: realm_access.roles werden zu ROLE_* in Großbuchstaben")
  void mapsRealmRoles() {
    var claims =
        Map.<String, Object>of("realm_access", Map.of("roles", List.of("customer", "staff")));

    var authorities =
        mapper.fromClaims(claims).stream().map(GrantedAuthority::getAuthority).toList();

    assertThat(authorities).containsExactlyInAnyOrder("ROLE_CUSTOMER", "ROLE_STAFF");
  }

  @Test
  @DisplayName("AUTH-U-01: Keycloak-Standardrolle default-roles-* wird nicht zur Fachrolle")
  void ignoresKeycloakDefaultRole() {
    var claims =
        Map.<String, Object>of(
            "realm_access", Map.of("roles", List.of("default-roles-fermate", "customer")));

    assertThat(mapper.fromClaims(claims))
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ROLE_CUSTOMER");
  }

  @Test
  @DisplayName("AUTH-U-01: Token ohne realm_access ergibt keine Rollen statt eines Fehlers")
  void toleratesMissingClaim() {
    assertThat(mapper.fromClaims(Map.of("sub", "abc"))).isEmpty();
    assertThat(mapper.fromClaims(Map.of("realm_access", "kaputt"))).isEmpty();
  }
}
