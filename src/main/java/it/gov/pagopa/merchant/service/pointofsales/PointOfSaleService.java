package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.model.PointOfSale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PointOfSaleService {

    void savePointOfSales(String merchantId, List<PointOfSale> pointOfSaleList);

    Page<PointOfSale> getPointOfSalesList(String merchantId, String type, String city, String address, String contactName, Pageable pageable);

    PointOfSale getPointOfSaleById(String pointOfSaleId);
}
