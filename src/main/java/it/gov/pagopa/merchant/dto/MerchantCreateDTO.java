package it.gov.pagopa.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantCreateDTO {

  @NotBlank
  private String acquirerId;

  @NotBlank
  private String businessName;

  @NotBlank
  private String fiscalCode;

  private String iban;

  private String ibanHolder;

  private LocalDateTime activationDate;
}
