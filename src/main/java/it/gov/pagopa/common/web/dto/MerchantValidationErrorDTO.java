package it.gov.pagopa.common.web.dto;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class MerchantValidationErrorDTO extends ErrorDTO {

  private List<MerchantValidationErrorDetail> details;
}
