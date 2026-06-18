package it.gov.pagopa.merchant.connector.pdnd.dto;

import it.gov.pagopa.merchant.connector.pdnd.dto.JwtConfig;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PdndSecretValue {
    private JwtConfig jwtConfig;
    private String clientId;
    private String secretKey;
}
