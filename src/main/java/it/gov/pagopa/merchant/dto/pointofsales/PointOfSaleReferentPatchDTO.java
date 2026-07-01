package it.gov.pagopa.merchant.dto.pointofsales;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointOfSaleReferentPatchDTO {

    @NotBlank
    @Email
    @JsonProperty("contactEmail")
    private String contactEmail;

    @NotBlank
    @JsonProperty("contactName")
    private String contactName;

    @NotBlank
    @JsonProperty("contactSurname")
    private String contactSurname;
}
