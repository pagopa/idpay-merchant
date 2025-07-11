package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleFilteredDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSalePage;

import java.util.List;

public interface PointOfSaleService {

    void savePointOfSales(String merchantId, List<PointOfSaleDTO> pointOfSaleDTOList);

    PointOfSalePage getPointOfSales(PointOfSaleFilteredDTO filter);

}
