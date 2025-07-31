package it.gov.pagopa.merchant.keycloak.configuration;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminClientConfig {

  @Value("${keycloak.admin.server-url}")
  private String serverUrl;

  @Value("${keycloak.admin.realm}")
  private String realm;

  @Value("${keycloak.admin.client-id}")
  private String clientId;

  @Value("${keycloak.admin.client-secret}")
  private String clientSecret;

  /**
   * Creates and configures a Keycloak Admin Client SDK instance.
   * Grant type: client-credentials
   *
   * @return Keycloak instance.
   */
  @Bean
  public Keycloak keycloakAdminClient() {
    return KeycloakBuilder.builder()
        .serverUrl(serverUrl)
        .realm(realm)
        .grantType("client_credentials")
        .clientId(clientId)
        .clientSecret(clientSecret)
        .build();
  }
}
