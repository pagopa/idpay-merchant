package it.gov.pagopa.merchant.dto.pointofsales;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointOfSaleDetailDTO {

    private PointOfSaleDTO pointOfSale;
    private MerchantDetailDTO merchant;
}
