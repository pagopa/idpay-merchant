package it.gov.pagopa.merchant.dto.pointofsales;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PointOfSaleExclusionResultDTO {
    private List<ExcludedPointOfSaleDetailDTO> excludedPointOfSales;
    private List<NotExcludedPointOfSaleDTO> notExcludedPointOfSales;
}
