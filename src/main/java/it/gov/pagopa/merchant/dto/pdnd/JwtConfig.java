package it.gov.pagopa.merchant.dto.pdnd;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtConfig {
    private String issuer;
    private String subject;
    private String audience;
    private String kid;
    private String purposeId;

}
