package it.gov.pagopa.merchant.dto.pointofsales;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.utils.validator.ValidationApiEnabledGroup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PointOfSaleDTO {

    private static final String VALID_LINK = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    @NotNull
    @JsonProperty("type")
    private PointOfSaleTypeEnum type;

    @NotBlank
    @JsonProperty("franchiseName")
    private String franchiseName;

    @JsonProperty("region")
    private String region;

    @JsonProperty("province")
    private String province;

    @JsonProperty("city")
    private String city;

    @JsonProperty("zipCode")
    private String zipCode;

    @JsonProperty("address")
    private String address;

    @JsonProperty("streetNumber")
    private String streetNumber;

    @URL(protocol = "https", regexp = VALID_LINK, groups = ValidationApiEnabledGroup.class)
    @JsonProperty("website")
    private String website;

    @JsonProperty("contactEmail")
    private String contactEmail;

    @JsonProperty("contactName")
    private String contactName;

    @JsonProperty("contactSurname")
    private String contactSurname;

    @JsonProperty("channels")
    @Valid
    private List<ChannelDTO> channels;
}
