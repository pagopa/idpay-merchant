package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.model.PointOfSale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetPointOfSaleService {

    Page<PointOfSale> getPointOfSalesList(String merchantId, String type, String city, String address, String contactName, Pageable pageable);

    PointOfSale getPointOfSaleById(String pointOfSaleId);

    PointOfSale getPointOfSaleByIdAndMerchantId(String pointOfSaleId, String merchantId);

}
