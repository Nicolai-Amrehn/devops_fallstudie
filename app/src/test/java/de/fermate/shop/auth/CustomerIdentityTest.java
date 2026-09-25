package de.fermate.shop.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CustomerIdentityTest {

  @Test
  @DisplayName("AUTH-U-02: sub wird Kundennummer, name wird Anzeigename")
  void usesSubAndName() {
    var identity =
        CustomerIdentity.from(
            Map.of("sub", "42-abc", "name", "Anna Beispiel", "email", "a@x.test"));

    assertThat(identity.customerId()).isEqualTo("42-abc");
    assertThat(identity.displayName()).isEqualTo("Anna Beispiel");
  }

  @Test
  @DisplayName("AUTH-U-02: ohne name: preferred_username, dann email, zuletzt sub")
  void fallsBackForDisplayName() {
    assertThat(
            CustomerIdentity.from(Map.of("sub", "1", "preferred_username", "anna")).displayName())
        .isEqualTo("anna");
    assertThat(CustomerIdentity.from(Map.of("sub", "1", "email", "a@x.test")).displayName())
        .isEqualTo("a@x.test");
    assertThat(CustomerIdentity.from(Map.of("sub", "1")).displayName()).isEqualTo("1");
  }

  @Test
  @DisplayName("AUTH-U-02: Token ohne sub wird abgelehnt")
  void rejectsMissingSub() {
    assertThatThrownBy(() -> CustomerIdentity.from(Map.of("name", "Niemand")))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
