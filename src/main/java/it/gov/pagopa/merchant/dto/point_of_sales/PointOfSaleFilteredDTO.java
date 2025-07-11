package it.gov.pagopa.merchant.dto.point_of_sales;

import lombok.*;
import org.springframework.data.domain.Pageable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointOfSaleFilteredDTO {

    private String merchantId;

    private String type;
    private String city;
    private String address;
    private String contactName;

    private Pageable pageable;
}
