package it.gov.pagopa.merchant.service.sale;

import it.gov.pagopa.merchant.dto.sale.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.sale.PointOfSaleFilteredDTO;

import java.util.List;

public interface PointOfSaleService {

    void savePointOfSales(String merchantId, List<PointOfSaleDTO> pointOfSaleDTOList);

    List<PointOfSaleDTO> getPointOfSales(PointOfSaleFilteredDTO filter);

}
