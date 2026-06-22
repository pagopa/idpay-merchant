package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.model.PointOfSale;

public interface UpdatePointOfSaleService {

    PointOfSale patchPointOfSale(String pointOfSaleId, String merchantId, PointOfSaleDTO pointOfSaleDTO);
}
