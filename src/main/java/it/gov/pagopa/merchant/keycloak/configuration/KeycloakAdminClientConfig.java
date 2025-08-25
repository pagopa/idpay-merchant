package it.gov.pagopa.merchant.keycloak.configuration;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminClientConfig {

  private final KeycloakAdminProperties keycloakAdminProperties;

  public KeycloakAdminClientConfig(KeycloakAdminProperties keycloakAdminProperties) {
    this.keycloakAdminProperties = keycloakAdminProperties;
  }

  /**
   * Creates and configures a Keycloak Admin Client SDK instance.
   * Grant type: client-credentials
   *
   * @return Keycloak instance.
   */
  @Bean
  public Keycloak keycloakAdminClient() {
    return KeycloakBuilder.builder()
        .serverUrl(keycloakAdminProperties.getServerUrl())
        .realm(keycloakAdminProperties.getRealm())
        .grantType("client_credentials")
        .clientId(keycloakAdminProperties.getClientId())
        .clientSecret(keycloakAdminProperties.getClientSecret())
        .build();
  }
}
