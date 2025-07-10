package it.gov.pagopa.merchant.dto.sale;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.merchant.dto.enums.SaleTypeEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointOfSaleDTO {

    @NotBlank
    @JsonProperty("saleType")
    private SaleTypeEnum saleType;

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

    @JsonProperty("website")
    private String website;

    @JsonProperty("contactEmail")
    private String contactEmail;

    @JsonProperty("contactName")
    private String contactName;

    @JsonProperty("contactSurname")
    private String contactSurname;

    @JsonProperty("channels")
    private List<ChannelDTO> channels;
}
