package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.merchant.dto.sale.ListPointOfSaleDTO;
import it.gov.pagopa.merchant.dto.sale.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.sale.PointOfSaleFilteredDTO;
import it.gov.pagopa.merchant.service.sale.PointOfSaleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class PointOfSalesControllerImpl implements PointOfSalesController {

  private final PointOfSaleService pointOfSaleService;

  public PointOfSalesControllerImpl(PointOfSaleService pointOfSaleService) {
    this.pointOfSaleService = pointOfSaleService;
  }


  @Override
  public ResponseEntity<Void> savePointOfSales(String merchantId, List<PointOfSaleDTO> pointOfSales){
    log.info("[POINT-OF-SALE][SAVE] Received request to save {} point(s) for merchantId={}",pointOfSales.size(),merchantId);

    if(pointOfSales.isEmpty()){
      log.warn("[POINT-OF-SALE][SAVE] Point of sales list is empty for merchantId={}",merchantId);
      throw new InvalidRequestException("Point of sales list cannot be empty.");
    }
    
    pointOfSaleService.savePointOfSales(merchantId, pointOfSales);

    log.info("[POINT-OF-SALE][SAVE] Successfully saved point of sales for merchantId={}",merchantId);
    return ResponseEntity.noContent().build();
  }
  
  @Override
  public ResponseEntity<ListPointOfSaleDTO> getPointOfSales(String merchantId,
                                                            String type, String  city, String  address, String contactName,
                                                            Pageable pageable){
    log.info("[POINT-OF-SALE][GET] Received GET request for point of sales list - merchantId={}",merchantId);

    PointOfSaleFilteredDTO pointOfSaleFilteredDTO = PointOfSaleFilteredDTO.builder()
            .merchantId(merchantId)
            .type(type)
            .city(city)
            .address(address)
            .contactName(contactName)
            .pageable(pageable)
            .build();

    List<PointOfSaleDTO> list = pointOfSaleService.getPointOfSales(pointOfSaleFilteredDTO);

    log.info("[POINT-OF-SALE][GET] Returning {} point(s) for merchantId={}",list.size(), merchantId);

    return ResponseEntity.ok(ListPointOfSaleDTO.builder().pointOfSale(list).pageSize(pageable.getPageSize()).pageNumber(pageable.getPageNumber()).build());
  }



}
