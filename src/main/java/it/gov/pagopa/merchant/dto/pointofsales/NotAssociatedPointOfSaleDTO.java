package it.gov.pagopa.merchant.dto.pointofsales;


import it.gov.pagopa.merchant.dto.enums.PosOnbordingRejectionReason;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class NotAssociatedPointOfSaleDTO {

    private String pointOfSaleId;
    private String pointOfSaleName;
    private PosOnbordingRejectionReason reason;
    private String city;
    private String address;
    private String streetNumber;

}