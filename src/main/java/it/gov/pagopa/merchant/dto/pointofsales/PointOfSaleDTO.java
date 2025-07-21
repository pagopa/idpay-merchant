package it.gov.pagopa.merchant.dto.pointofsales;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PointOfSaleDTO {

    @JsonProperty("id")
    private String id;

    @NotNull
    @JsonProperty("type")
    private PointOfSaleTypeEnum type;

    @NotBlank
    @JsonProperty("franchiseName")
    private String franchiseName;

    @NotNull(message = "Region must not be null")
    @JsonProperty("region")
    private String region;

    @NotNull(message = "Province must not be null")
    @JsonProperty("province")
    private String province;

    @NotNull(message = "City must not be null")
    @JsonProperty("city")
    private String city;

    @NotNull(message = "ZipCode must not be null")
    @JsonProperty("zipCode")
    private String zipCode;

    @NotNull(message = "Address must not be null")
    @JsonProperty("address")
    private String address;

    @NotNull(message = "StreetNumber must not be null")
    @JsonProperty("streetNumber")
    private String streetNumber;

    @NotNull(message = "website must not be null")
    @JsonProperty("website")
    private String website;

    @NotNull(message = "contactEmail must not be null")
    @JsonProperty("contactEmail")
    private String contactEmail;

    @NotNull(message = "contactName must not be null")
    @JsonProperty("contactName")
    private String contactName;

    @NotNull(message = "contactSurname must not be null")
    @JsonProperty("contactSurname")
    private String contactSurname;

    @JsonProperty("channels")
    @Valid
    private List<ChannelDTO> channels;
}
