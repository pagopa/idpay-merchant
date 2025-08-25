package it.gov.pagopa.merchant.dto.pointofsales;

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
}
