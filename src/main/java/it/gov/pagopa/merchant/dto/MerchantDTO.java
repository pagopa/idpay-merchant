package it.gov.pagopa.merchant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantDTO {
    private String merchantId;
    private String businessName;
    private String fiscalCode;
    private String merchantStatus;
    private String updateStatusDate;
}
