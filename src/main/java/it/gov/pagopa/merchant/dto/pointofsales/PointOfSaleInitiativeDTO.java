package it.gov.pagopa.merchant.dto.pointofsales;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointOfSaleInitiativeDTO {

    private String initiativeId;
    private Instant createdAt;
}
