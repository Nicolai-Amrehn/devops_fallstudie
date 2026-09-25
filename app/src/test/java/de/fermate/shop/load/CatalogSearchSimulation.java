package de.fermate.shop.load;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;

/**
 * LOAD-01: Katalogsuche und Artikelseite unter Last. Schwellwerte: p95 unter 500 ms, keine Fehler.
 *
 * <p>Start: {@code mvn gatling:test -Dgatling.baseUrl=http://localhost:8080}
 */
public class CatalogSearchSimulation extends Simulation {

  private final String baseUrl = System.getProperty("gatling.baseUrl", "http://localhost:8080");
  private final int usersPerSecond = Integer.getInteger("gatling.usersPerSecond", 20);
  private final int durationSeconds = Integer.getInteger("gatling.durationSeconds", 30);

  private final HttpProtocolBuilder protocol =
      http.baseUrl(baseUrl).acceptHeader("text/html").userAgentHeader("gatling-fallstudie");

  private final ScenarioBuilder browseCatalog =
      scenario("Katalog durchsuchen")
          .feed(csv("load/searches.csv").circular())
          .exec(http("Startseite").get("/").check(status().is(200)))
          .pause(Duration.ofMillis(300), Duration.ofMillis(900))
          .exec(http("Suche").get("/?q=#{query}").check(status().is(200)))
          .pause(Duration.ofMillis(300), Duration.ofMillis(900))
          .exec(http("Artikelseite").get("/products/#{productId}").check(status().is(200)));

  {
    setUp(
            browseCatalog.injectOpen(
                atOnceUsers(5),
                rampUsersPerSec(1).to(usersPerSecond).during(Duration.ofSeconds(durationSeconds))))
        .protocols(protocol)
        .assertions(
            global().responseTime().percentile(95.0).lt(500),
            global().failedRequests().percent().is(0.0));
  }
}
