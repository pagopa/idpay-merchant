package it.gov.pagopa.merchant.dto.pointofsales;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.merchant.dto.enums.ChannelTypeEnum;
import it.gov.pagopa.merchant.utils.validator.ValidationApiEnabledGroup;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelDTO {

  @JsonProperty("type")
  @NotNull(groups = ValidationApiEnabledGroup.class)
  private ChannelTypeEnum type;

  @JsonProperty("contact")
  @NotNull(groups = ValidationApiEnabledGroup.class)
  private String contact;

  private static final String VALID_WEB = "^https://[-a-zA-Z0-9+&@#/%?=|!:,.;]*[-a-zA-Z0-9+&@#/%=|]$";
  private static final String VALID_EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
  private static final String VALID_MOBILE = "^\\+?[0-9]{7,15}$";
  private static final String VALID_LANDING = "^https://[-a-zA-Z0-9+&@#/%?=|!:,.;]*[-a-zA-Z0-9+&@#/%=|]$";

  @AssertTrue(message = "Invalid contact format for the web type", groups = ValidationApiEnabledGroup.class)
  public boolean isContactValid() {
    if (contact == null || type == null) return true;

    return switch (type) {
      case WEB -> contact.matches(VALID_WEB);
      case EMAIL -> contact.matches(VALID_EMAIL);
      case MOBILE -> contact.matches(VALID_MOBILE);
      case LANDING -> contact.matches(VALID_LANDING);
      default -> true;
    };
  }
}
