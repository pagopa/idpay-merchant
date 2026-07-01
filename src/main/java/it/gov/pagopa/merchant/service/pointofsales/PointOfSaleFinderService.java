package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.model.PointOfSale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PointOfSaleFinderService {

    Page<PointOfSale> getPointOfSalesList(String merchantId, String type, String city, String address, String contactName, Pageable pageable);



    PointOfSale getPointOfSaleByIdAndMerchantId(String pointOfSaleId, String merchantId);

}
