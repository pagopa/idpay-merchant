package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;

import java.util.List;

public interface SavePointOfSaleService {

    void savePointOfSales(String merchantId, String initiativeId, List<PointOfSaleDTO>  pointOfSaleList);


}
