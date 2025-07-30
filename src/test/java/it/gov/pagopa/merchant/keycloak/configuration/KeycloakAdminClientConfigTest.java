package it.gov.pagopa.merchant.keycloak.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(
    classes = {
        KeycloakAdminClientConfig.class
    })
class KeycloakAdminClientConfigTest {

  @InjectMocks
  private KeycloakAdminClientConfig keycloakAdminClientConfig;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(keycloakAdminClientConfig, "serverUrl", "http://localhost:8080/auth");
    ReflectionTestUtils.setField(keycloakAdminClientConfig, "realm", "test-realm");
    ReflectionTestUtils.setField(keycloakAdminClientConfig, "clientId", "test-client");
    ReflectionTestUtils.setField(keycloakAdminClientConfig, "clientSecret", "test-secret");
  }

  @Test
  void keycloakAdminClient() {
    Keycloak keycloak = keycloakAdminClientConfig.keycloakAdminClient();
    assertNotNull(keycloak);
  }
}