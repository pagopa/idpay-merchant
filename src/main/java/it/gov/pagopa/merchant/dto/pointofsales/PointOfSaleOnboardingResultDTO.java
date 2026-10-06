package it.gov.pagopa.merchant.dto.pointofsales;

import lombok.Data;

import java.util.List;

@Data
public class PointOfSaleOnboardingResultDTO {

    private List<AssociatedPointOfSaleDTO> associated;
    private List<NotAssociatedPointOfSaleDTO> notAssociated;

}
