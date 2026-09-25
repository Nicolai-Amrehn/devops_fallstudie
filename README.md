# Fermate-Webshop: Teststrategie und CI-Pipeline

Proof of Concept zur Fallstudie im Kurs DLBSEPDOCD01_D (DevOps und Continuous
Delivery), Aufgabenstellung 2: automatisierte Tests für eine Webanwendung.

Die Anwendung ist ein modularer Monolith (Java 21, Spring Boot, PostgreSQL) mit
den Modulen Auth, Katalog und Bestellung. Sie bindet Keycloak über OpenID
Connect und einen Zahlungsdienstleister an, der im PoC durch WireMock-Stubs
vertreten wird.

## Aufbau

| Pfad | Inhalt |
| --- | --- |
| `app/src/main` | Anwendung |
| `app/src/test` | Unit-Tests (`*Test`), Integrationstests (`*IT`), Lasttest (Gatling) |
| `app/e2e` | End-to-End-Tests (Playwright) |
| `app/wiremock` | Stubs des Zahlungsdienstleisters für Integrationstests und Compose |
| `app/keycloak` | Realm-Export für Compose und Integrationstests |
| `.github/workflows` | CI-Pipeline (`ci.yml`) und nächtlicher Lasttest (`nightly.yml`) |

## Pipeline

`lint` → `unit` → `integration` → `build-image` → `e2e` → `report`

Das Image wird erst nach bestandenen Unit- und Integrationstests gebaut; die
E2E-Tests laufen gegen dieses Image. Das Repository-Secret
`PAYMENT_CALLBACK_TOKEN` muss gesetzt sein.

## Lokal ausführen

Voraussetzungen: JDK 21, Maven, Docker.

```bash
cd app
mvn test          # Unit-Tests
mvn verify        # zusätzlich Integrationstests mit Testcontainers
```

End-to-End-Tests gegen den Compose-Stack:

```bash
cd app
cp .env.example .env
mvn package -DskipTests
docker build -t ghcr.io/nicolai-amrehn/devops_fallstudie/fermate-shop:local .
docker compose up --detach --wait webshop
docker compose --profile e2e run --rm e2e
docker compose --profile e2e down --volumes
```
