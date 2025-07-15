package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleListDTO;
import it.gov.pagopa.merchant.service.pointofsales.PointOfSaleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class PointOfSaleControllerImpl implements PointOfSaleController{

  private final PointOfSaleService pointOfSaleService;

  public PointOfSaleControllerImpl(PointOfSaleService pointOfSaleService) {
    this.pointOfSaleService = pointOfSaleService;
  }

  @Override
  public ResponseEntity<Void> savePointOfSales(String merchantId, List<PointOfSaleDTO> pointOfSales){
    if(pointOfSales == null || pointOfSales.isEmpty()){
      log.warn("[POINT-OF-SALE][SAVE] Point of sales list is empty for merchantId={}",merchantId);
      throw new ClientExceptionWithBody(
              HttpStatus.BAD_REQUEST,
              MerchantConstants.ExceptionCode.POINT_OF_SALE_BAD_REQUEST,
              "Point of sales list cannot be empty.");
    }
    log.info("[POINT-OF-SALE][SAVE] Received request to save {} point(s) for merchantId={}",pointOfSales.size(),merchantId);

    pointOfSaleService.savePointOfSales(merchantId, pointOfSales);

    log.info("[POINT-OF-SALE][SAVE] Successfully saved point of sales for merchantId={}",merchantId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<PointOfSaleListDTO> getPointOfSalesList(String merchantId, String type, String city, String address, String contactName, Pageable pageable) {
    log.info("[POINT-OF-SALE][GET] Retrieve request for merchantId={}",merchantId);

    PointOfSaleListDTO pointOfSaleListDTO = pointOfSaleService.getPointOfSalesList(merchantId, type, city, address, contactName, pageable);

    log.info("[POINT-OF-SALE][GET] Retrieved {} point fo sales for merchantId={}",pointOfSaleListDTO.getContent().size(), merchantId);

    return ResponseEntity.ok(pointOfSaleListDTO);
  }
  

}
