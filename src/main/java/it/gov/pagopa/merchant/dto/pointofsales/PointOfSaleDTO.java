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
public class PointOfSaleDTO {

    @JsonProperty("id")
    private String id;

    @NotNull
    @JsonProperty("type")
    private PointOfSaleTypeEnum type;

    @NotBlank
    @JsonProperty("franchiseName")
    private String franchiseName;

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

    @NotBlank(message = "website must not be blank", groups = OnlineGroup.class)
    @JsonProperty("website")
    private String website;

    @NotBlank(message = "contactEmail must not be blank", groups = {PhysicalGroup.class, OnlineGroup.class})
    @JsonProperty("contactEmail")
    private String contactEmail;

    @NotNull(message = "contactName must not be null")
    @JsonProperty("contactName")
    private String contactName;

    @NotNull(message = "contactSurname must not be null")
    @JsonProperty("contactSurname")
    private String contactSurname;

    @NotNull(message = "channelEmail must not be null", groups = PhysicalGroup.class)
    @JsonProperty("channelEmail")
    private String channelEmail;

    @NotNull(message = "channelPhone must not be null", groups = PhysicalGroup.class)
    @JsonProperty("channelPhone")
    private String channelPhone;


    @NotNull(message = "channelGeolink must not be null", groups = PhysicalGroup.class)
    @JsonProperty("channelGeolink")
    private String channelGeolink;

    @JsonProperty("businessName")
    private String businessName;

    @JsonProperty("fiscalCode")
    private String fiscalCode;

    @JsonProperty("vatNumber")
    private String vatNumber;

    @Deprecated
    @JsonProperty("channelWebsite")
    private String channelWebsite;

}
