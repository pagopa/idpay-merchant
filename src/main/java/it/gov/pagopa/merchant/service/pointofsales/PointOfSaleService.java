package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleListDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PointOfSaleService {

    void savePointOfSales(String merchantId, List<PointOfSaleDTO> pointOfSaleDTOList);

    PointOfSaleListDTO getPointOfSalesList(String merchantId, String type, String city, String address, String contactName, Pageable pageable);

}
