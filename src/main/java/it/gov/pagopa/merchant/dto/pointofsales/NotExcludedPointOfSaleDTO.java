package it.gov.pagopa.merchant.dto.pointofsales;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.gov.pagopa.merchant.dto.enums.PosOnbordingExclusionRejectionReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotExcludedPointOfSaleDTO {
    private String pointOfSaleId;
    private String franchiseName;
    private String type;
    private String address;
    private String streetNumber;
    private String city;
    private String website;
    private PosOnbordingExclusionRejectionReason reason;
}
