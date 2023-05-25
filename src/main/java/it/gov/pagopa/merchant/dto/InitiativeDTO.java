package it.gov.pagopa.merchant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * InitiativeDTO
 */
@Data
public class InitiativeDTO {

    @JsonProperty("initiativeId")
    private String initiativeId;

    @JsonProperty("initiativeName")
    private String initiativeName;

}