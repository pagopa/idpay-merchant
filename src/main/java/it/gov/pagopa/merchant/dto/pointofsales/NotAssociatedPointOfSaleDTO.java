package it.gov.pagopa.merchant.dto.pointofsales;


import com.fasterxml.jackson.annotation.JsonInclude;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.enums.PosOnbordingRejectionReason;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotAssociatedPointOfSaleDTO {

    private String pointOfSaleId;
    private String pointOfSaleName;
    private PosOnbordingRejectionReason reason;
    private String city;
    private String address;
    private String streetNumber;
    private String website;
    private PointOfSaleTypeEnum type;


}