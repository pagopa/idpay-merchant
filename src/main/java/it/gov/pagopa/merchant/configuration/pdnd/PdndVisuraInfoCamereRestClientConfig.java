package it.gov.pagopa.merchant.configuration.pdnd;

import it.gov.pagopa.merchant.dto.pdnd.JwtConfig;
import it.gov.pagopa.merchant.dto.pdnd.PdndSecretValue;
import it.gov.pagopa.merchant.connector.pdnd.rest.PdndVisuraInfoCamereRawRestClient;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;


@Getter
@Configuration
@EnableFeignClients(clients = { PdndVisuraInfoCamereRawRestClient.class })
public class PdndVisuraInfoCamereRestClientConfig {
    private final PdndSecretValue pdndSecretValue;

    public PdndVisuraInfoCamereRestClientConfig(
            @Value("${rest-client.pdnd-visura-infocamere.privateKey}") String privateKey,
            @Value("${rest-client.pdnd-visura-infocamere.clientId}") String clientId,
            @Value("${rest-client.pdnd-visura-infocamere.kid}") String kid,
            @Value("${rest-client.pdnd-visura-infocamere.audience}") String audience,
            @Value("${rest-client.pdnd-visura-infocamere.purposeId}") String purposeId
    ) {

        JwtConfig jwtConfig = JwtConfig.builder()
                .audience(audience)
                .issuer(clientId)
                .subject(clientId)
                .purposeId(purposeId)
                .kid(kid)
                .build();
        this.pdndSecretValue = PdndSecretValue.builder()
                .clientId(clientId)
                .secretKey(privateKey)
                .jwtConfig(jwtConfig)
                .build();
    }

}
