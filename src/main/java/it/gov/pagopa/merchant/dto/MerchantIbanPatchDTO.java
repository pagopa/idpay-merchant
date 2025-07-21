package it.gov.pagopa.merchant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantIbanPatchDTO {
  private String iban;
  private String ibanHolder; // intestatario iban
}
