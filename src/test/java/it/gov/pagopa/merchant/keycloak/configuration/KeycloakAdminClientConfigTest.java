package it.gov.pagopa.merchant.keycloak.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;

class KeycloakAdminClientConfigTest {

  private KeycloakAdminClientConfig keycloakAdminClientConfig;

  @BeforeEach
  void setUp() {
    KeycloakAdminProperties keycloakAdminProperties = new KeycloakAdminProperties();
    keycloakAdminProperties.setServerUrl("http://localhost:8080/auth");
    keycloakAdminProperties.setRealm("test-realm");
    keycloakAdminProperties.setClientId("test-client");
    keycloakAdminProperties.setClientSecret("test-secret");

    keycloakAdminClientConfig = new KeycloakAdminClientConfig(keycloakAdminProperties);
  }

  @Test
  void keycloakAdminClient() {
    Keycloak keycloak = keycloakAdminClientConfig.keycloakAdminClient();
    assertNotNull(keycloak);
  }
}