package it.gov.pagopa.merchant.dto.pointofsales;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.merchant.dto.enums.ChannelTypeEnum;
import it.gov.pagopa.merchant.utils.validator.ValidChannelContact;
import it.gov.pagopa.merchant.utils.validator.ValidationApiEnabledGroup;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidChannelContact(groups = ValidationApiEnabledGroup.class)
public class ChannelDTO {

  @NotNull(groups = ValidationApiEnabledGroup.class)
  @JsonProperty("type")
  private ChannelTypeEnum type;

  @NotNull(groups = ValidationApiEnabledGroup.class)
  @JsonProperty("contact")
  private String contact;

}
