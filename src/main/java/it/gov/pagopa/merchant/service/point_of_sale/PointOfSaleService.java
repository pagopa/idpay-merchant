package it.gov.pagopa.merchant.service.point_of_sale;

import it.gov.pagopa.merchant.dto.PointOfSaleListDTO;
import org.springframework.data.domain.Pageable;

public interface PointOfSaleService {
    PointOfSaleListDTO getPointOfSalesList(String merchantId, String type, String city, String address, String contactName, Pageable pageable);
}
