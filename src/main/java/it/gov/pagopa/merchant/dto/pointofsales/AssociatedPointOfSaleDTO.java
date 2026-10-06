package it.gov.pagopa.merchant.dto.pointofsales;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssociatedPointOfSaleDTO {

    private String pointOfSaleId;
    private String franchiseName;

}