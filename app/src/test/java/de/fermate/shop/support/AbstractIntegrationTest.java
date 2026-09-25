package de.fermate.shop.support;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Gemeinsame Infrastruktur der Integrationstests: PostgreSQL und Keycloak als Container, WireMock
 * für den Zahlungsdienstleister. Die Container starten einmal pro JVM; die Stubs kommen aus
 * demselben Verzeichnis, das auch der Compose-Stack einbindet.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

  public static final String CALLBACK_TOKEN = "it-callback-token";

  protected static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine");

  protected static final KeycloakContainer KEYCLOAK =
      new KeycloakContainer("quay.io/keycloak/keycloak:26.7.4")
          .withRealmImportFile("/fermate-realm.json");

  protected static final WireMockServer PAYMENT =
      new WireMockServer(
          wireMockConfig()
              .dynamicPort()
              .usingFilesUnderDirectory("wiremock")
              .globalTemplating(true));

  static {
    POSTGRES.start();
    KEYCLOAK.start();
    PAYMENT.start();
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add(
        "spring.security.oauth2.client.provider.keycloak.issuer-uri",
        () -> KEYCLOAK.getAuthServerUrl() + "/realms/fermate");
    registry.add("payment.base-url", PAYMENT::baseUrl);
    registry.add("payment.callback-token", () -> CALLBACK_TOKEN);
  }

  /** Programmatische Stubs einzelner Tests dürfen die Dateistubs nicht dauerhaft überlagern. */
  @BeforeEach
  void resetPaymentStubs() {
    PAYMENT.resetToDefaultMappings();
    PAYMENT.resetRequests();
  }
}
