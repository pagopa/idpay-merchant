package it.gov.pagopa.merchant.keycloak;


import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminClient {

    //TODO move to yaml
    private static final String REALM = "";
    private static final String CLIENT_ID = "";
    private static final String SERVERURL = "";
    private static final String CLIENT_SECRET = "";

    @Bean
    Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(SERVERURL)
                .realm(REALM)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .build();
    }
}