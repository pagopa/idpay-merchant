package it.gov.pagopa.merchant.connector.pdnd.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.codehaus.janino.TokenType;

@Data
public class ClientCredentialsResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private TokenType tokenType;

    @JsonProperty("expires_in")
    private Integer expiresIn;
}