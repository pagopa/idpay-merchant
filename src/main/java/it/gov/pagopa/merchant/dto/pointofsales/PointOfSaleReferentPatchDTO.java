package it.gov.pagopa.merchant.dto.pointofsales;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointOfSaleReferentPatchDTO {

    @JsonProperty("contactEmail")
    private String contactEmail;

    @JsonProperty("contactName")
    private String contactName;

    @JsonProperty("contactSurname")
    private String contactSurname;
}
