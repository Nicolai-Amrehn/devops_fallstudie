package de.fermate.shop.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Öffentlich: Katalog, Warenkorb, Zahlungs-Callback, Health. Geschützt: Checkout und Bestellungen.
 * Anmeldung ausschließlich über den externen Identity Provider (OpenID Connect).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http, RoleMapper roleMapper) throws Exception {
    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/", "/products/**", "/cart/**", "/css/**", "/actuator/health/**")
                    .permitAll()
                    .requestMatchers("/api/payments/callback")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2Login(login -> login.userInfoEndpoint(u -> u.userAuthoritiesMapper(roleMapper)))
        .logout(logout -> logout.logoutSuccessUrl("/"))
        // Der Zahlungsdienstleister ruft den Callback serverseitig auf; er besitzt kein
        // CSRF-Token. Der Endpunkt sichert sich stattdessen über ein gemeinsames Geheimnis.
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/payments/callback"));
    return http.build();
  }
}
