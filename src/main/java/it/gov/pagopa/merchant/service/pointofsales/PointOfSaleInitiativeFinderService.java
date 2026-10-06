package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.dto.enums.PointOfSaleInitiativeFilter;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleInitiativeListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PointOfSaleInitiativeFinderService {

    Page<PointOfSale> getPointOfSalesListByInitiative(String initiativeId, String merchantId, String type, String city, String address, String contactName, Pageable pageable);
    Page<PointOfSale> getPointOfSalesListByInitiativeFilter(PointOfSaleInitiativeFilter initiativeFilter, String merchantId, String type, String city, String address, String contactName, Pageable pageable);
    PointOfSale getPointOfSaleByIdAndMerchantIdAndInitiativeId(String initiativeId, String pointOfSaleId, String merchantId);
    PointOfSaleInitiativeListDTO getInitiativesByPointOfSaleIdAndMerchantId(String pointOfSaleId, String merchantId);
    PointOfSaleInitiativeListDTO getInitiativesByPointOfSaleId(String pointOfSaleId, String merchantId);
}
