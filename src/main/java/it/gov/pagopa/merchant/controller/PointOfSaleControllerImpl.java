package it.gov.pagopa.merchant.controller;


import it.gov.pagopa.merchant.dto.PointOfSaleListDTO;
import it.gov.pagopa.merchant.service.point_of_sale.PointOfSaleService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PointOfSaleControllerImpl implements PointOfSaleController{

    private final PointOfSaleService pointOfSaleService;

    public PointOfSaleControllerImpl(PointOfSaleService pointOfSaleService) {
        this.pointOfSaleService = pointOfSaleService;
    }

    @Override
    public ResponseEntity<PointOfSaleListDTO> getPointOfSalesList(String merchantId, String type, String city, String address, String contactName, Pageable pageable) {
        return ResponseEntity.ok(pointOfSaleService.getPointOfSalesList(merchantId, type, city, address, contactName, pageable));
    }
}
