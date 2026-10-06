package it.gov.pagopa.merchant.dto.pointofsales;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointOfSaleInitiativeListDTO {

    private List<PointOfSaleInitiativeDTO> initiatives;
}
