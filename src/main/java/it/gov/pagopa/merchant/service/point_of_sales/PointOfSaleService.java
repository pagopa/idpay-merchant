package it.gov.pagopa.merchant.service.point_of_sales;

import it.gov.pagopa.merchant.dto.point_of_sales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.point_of_sales.PointOfSaleFilteredDTO;
import it.gov.pagopa.merchant.dto.point_of_sales.PointOfSalePage;

import java.util.List;

public interface PointOfSaleService {

    void savePointOfSales(String merchantId, List<PointOfSaleDTO> pointOfSaleDTOList);

    PointOfSalePage getPointOfSales(PointOfSaleFilteredDTO filter);

}
