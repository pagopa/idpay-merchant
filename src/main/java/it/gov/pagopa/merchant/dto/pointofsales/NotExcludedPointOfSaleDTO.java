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
    private PosOnbordingExclusionRejectionReason reason;
}
