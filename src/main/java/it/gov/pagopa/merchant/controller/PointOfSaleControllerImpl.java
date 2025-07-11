package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.service.pointofsales.PointOfSaleService;
import it.gov.pagopa.merchant.utils.validator.ValidationApiEnabledGroup;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
public class PointOfSaleControllerImpl implements PointOfSaleController {

  private final PointOfSaleService pointOfSaleService;
  private final Validator validator;

  public PointOfSaleControllerImpl(PointOfSaleService pointOfSaleService,
                                   Validator validator) {
    this.pointOfSaleService = pointOfSaleService;
    this.validator = validator;
  }


  @Override
  public ResponseEntity<Void> savePointOfSales(String merchantId, List<PointOfSaleDTO> pointOfSales){
    log.info("[POINT-OF-SALE][SAVE] Received request to save {} point(s) for merchantId={}",pointOfSales.size(),merchantId);

    checkViolantions(pointOfSales);

    if(pointOfSales.isEmpty()){
      log.warn("[POINT-OF-SALE][SAVE] Point of sales list is empty for merchantId={}",merchantId);
      throw new InvalidRequestException("Point of sales list cannot be empty.");
    }
    
    pointOfSaleService.savePointOfSales(merchantId, pointOfSales);

    log.info("[POINT-OF-SALE][SAVE] Successfully saved point of sales for merchantId={}",merchantId);
    return ResponseEntity.noContent().build();
  }

  private void checkViolantions(List<PointOfSaleDTO> pointOfSaleDTOS){
    pointOfSaleDTOS.forEach(pointOfSaleDTO -> {
      Set<ConstraintViolation<PointOfSaleDTO>> violations = validator.validate(pointOfSaleDTO, ValidationApiEnabledGroup.class);
      if( !violations.isEmpty()){ throw new ConstraintViolationException(violations);}
    });
  }


}
