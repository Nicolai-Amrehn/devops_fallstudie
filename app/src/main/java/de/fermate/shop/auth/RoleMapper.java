package de.fermate.shop.auth;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.stereotype.Component;

/**
 * Übersetzt die Realm-Rollen aus dem ID-Token des Identity Providers ({@code realm_access.roles})
 * in Spring-Security-Rollen ({@code ROLE_*}). Unbekannte Strukturen führen nicht zu Fehlern,
 * sondern zu einer leeren Rollenmenge.
 */
@Component
public class RoleMapper implements GrantedAuthoritiesMapper {

  static final String REALM_ACCESS = "realm_access";
  static final String ROLES = "roles";

  @Override
  public Collection<? extends GrantedAuthority> mapAuthorities(
      Collection<? extends GrantedAuthority> authorities) {
    Set<GrantedAuthority> mapped = new HashSet<>();
    for (GrantedAuthority authority : authorities) {
      if (authority instanceof OidcUserAuthority oidc) {
        mapped.addAll(fromClaims(oidc.getIdToken().getClaims()));
      }
    }
    return mapped;
  }

  /** Reiner Kern ohne Spring-Security-Objekte – deshalb direkt testbar. */
  public Set<GrantedAuthority> fromClaims(Map<String, Object> claims) {
    Object realmAccess = claims.get(REALM_ACCESS);
    if (!(realmAccess instanceof Map<?, ?> access)) {
      return Set.of();
    }
    Object roles = access.get(ROLES);
    if (!(roles instanceof List<?> list)) {
      return Set.of();
    }
    Set<GrantedAuthority> result = new HashSet<>();
    for (Object role : list) {
      if (role instanceof String name && !name.isBlank() && !name.startsWith("default-roles-")) {
        result.add(new SimpleGrantedAuthority("ROLE_" + name.toUpperCase(Locale.ROOT)));
      }
    }
    return result;
  }
}
