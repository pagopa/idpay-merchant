package it.gov.pagopa.merchant.connector.pdnd.config;


import it.gov.pagopa.merchant.connector.pdnd.dto.PdndSecretValue;
import it.gov.pagopa.merchant.connector.pdnd.dto.JwtConfig;
import it.gov.pagopa.merchant.connector.pdnd.extra.rest.PDNDInfoCamereRestClient;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@EnableFeignClients(clients = PDNDInfoCamereRestClient.class )
public class PDNDInfoCamereRestClientConfig {
    private final PdndSecretValue pdndSecretValue;
    public PDNDInfoCamereRestClientConfig(
            @Value("${rest-client.pdnd-infocamere.privateKey}") String privateKey,
            @Value("${rest-client.pdnd-infocamere.clientId}") String clientId,
            @Value("${rest-client.pdnd-infocamere.kid}") String kid,
            @Value("${rest-client.pdnd-infocamere.audience}") String audience,
            @Value("${rest-client.pdnd-infocamere.purposeId}") String purposeId
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
