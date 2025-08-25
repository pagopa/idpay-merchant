package it.gov.pagopa.merchant.keycloak.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "keycloak.admin")
@Data
public class KeycloakAdminProperties {

  private String serverUrl;
  private String realm;
  private String clientId;
  private String clientSecret;
}
