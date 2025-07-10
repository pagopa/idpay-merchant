package it.gov.pagopa.merchant.dto.sale;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.merchant.dto.enums.ChannelTypeEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelDTO {

  @NotBlank
  @JsonProperty("type")
  private ChannelTypeEnum type;

  @NotBlank
  @JsonProperty("contact")
  private String contact;
}
