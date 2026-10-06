package it.gov.pagopa.merchant.dto.pdnd;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PdndSecretValue {
    private JwtConfig jwtConfig;
    private String clientId;
    private String secretKey;
}
