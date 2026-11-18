package it.gov.pagopa.merchant.dto.pointofsales;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.utils.validator.OnlineGroup;
import it.gov.pagopa.merchant.utils.validator.PhysicalGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PointOfSaleUpdateDTO {

    @NotBlank(message = "Region must not be blank", groups = PhysicalGroup.class)
    @JsonProperty("region")
    private String region;

    @NotBlank(message = "Province must not be blank", groups = PhysicalGroup.class)
    @JsonProperty("province")
    private String province;

    @NotBlank(message = "City must not be blank", groups = PhysicalGroup.class)
    @JsonProperty("city")
    private String city;

    @NotBlank(message = "ZipCode must not be blank", groups = PhysicalGroup.class)
    @JsonProperty("zipCode")
    private String zipCode;

    @NotBlank(message = "Address must not be blank", groups = PhysicalGroup.class)
    @JsonProperty("address")
    private String address;

    @NotBlank(message = "Street Number must not be blank", groups = PhysicalGroup.class)
    @JsonProperty("streetNumber")
    private String streetNumber;

    @NotBlank(message = "website must not be blank", groups = OnlineGroup.class)
    @JsonProperty("website")
    private String website;

    @NotNull(message = "channelEmail must not be null", groups = PhysicalGroup.class)
    @JsonProperty("channelEmail")
    private String channelEmail;

    @NotNull(message = "channelPhone must not be null", groups = PhysicalGroup.class)
    @JsonProperty("channelPhone")
    private String channelPhone;


    @NotNull(message = "channelGeolink must not be null", groups = PhysicalGroup.class)
    @JsonProperty("channelGeolink")
    private String channelGeolink;

}
