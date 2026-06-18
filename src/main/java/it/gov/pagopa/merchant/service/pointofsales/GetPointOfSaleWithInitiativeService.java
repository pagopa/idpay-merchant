package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.model.PointOfSale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetPointOfSaleWithInitiativeService {

    Page<PointOfSale> getPointOfSalesListByInitiative(String initiativeId, String merchantId, String type, String city, String address, String contactName, Pageable pageable);
    PointOfSale getPointOfSaleByIdAndMerchantIdAndInitiativeId(String initiativeId, String pointOfSaleId, String merchantId);
}
