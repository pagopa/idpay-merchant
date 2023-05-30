package it.gov.pagopa.merchant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MerchantDetailBaseDTO {
    private String fiscalCode;
    private String vatNumber;
}
