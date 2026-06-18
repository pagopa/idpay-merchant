package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleListDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotAllowedException;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleNotAllowedException;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.service.pointofsales.SavePointOfSaleService;
import it.gov.pagopa.merchant.service.pointofsales.GetPointOfSaleService;
import it.gov.pagopa.merchant.service.pointofsales.GetPointOfSaleWithInitiativeService;
import it.gov.pagopa.merchant.utils.Utilities;
import it.gov.pagopa.merchant.utils.validator.PointOfSaleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static it.gov.pagopa.merchant.utils.Utilities.sanitizeString;

@Slf4j
@RestController
public class PointOfSaleControllerImpl implements PointOfSaleController {

  private final SavePointOfSaleService savePointOfSaleService;
  private final GetPointOfSaleService getPointOfSaleService;
  private final GetPointOfSaleWithInitiativeService getPointOfSaleWithInitiativeService;
  private final PointOfSaleValidator pointOfSaleValidator;
  private final PointOfSaleDTOMapper pointOfSaleDTOMapper;
  private final MerchantService merchantService;
  private static final String MERCHANT_MISMATCH_MSG = "Merchant mismatch: expected [%s], but received [%s]";

  public PointOfSaleControllerImpl(SavePointOfSaleService savePointOfSaleService,
                                   GetPointOfSaleService getPointOfSaleService,
                                   GetPointOfSaleWithInitiativeService getPointOfSaleWithInitiativeService,
                                   PointOfSaleValidator pointOfSaleValidator,
                                   PointOfSaleDTOMapper pointOfSaleDTOMapper,
                                   MerchantService merchantService) {
    this.getPointOfSaleService = getPointOfSaleService;
    this.getPointOfSaleWithInitiativeService = getPointOfSaleWithInitiativeService;
    this.savePointOfSaleService = savePointOfSaleService;
    this.pointOfSaleValidator = pointOfSaleValidator;
    this.pointOfSaleDTOMapper = pointOfSaleDTOMapper;
    this.merchantService = merchantService;
  }


  @Override
  public ResponseEntity<Void> savePointOfSales(String merchantId, String initiativeId, String tokenMerchantId, List<PointOfSaleDTO> pointOfSales) {

    pointOfSaleValidator.validatePointOfSales(pointOfSales);
    pointOfSaleValidator.validateViolationsPointOfSales(pointOfSales);

    String sanitizedMerchantId = sanitizeString(merchantId);
    log.info("[POINT-OF-SALES][SAVE] Saving {} point(s) of sale for merchantId={}",
        pointOfSales.size(), sanitizedMerchantId);

    if (tokenMerchantId != null &&
        !Utilities.sanitizeString(tokenMerchantId).equals(sanitizedMerchantId)) {

      throw new MerchantNotAllowedException(MERCHANT_MISMATCH_MSG.formatted(tokenMerchantId, sanitizedMerchantId)
      );
    }

    savePointOfSaleService.savePointOfSales(sanitizedMerchantId,initiativeId, pointOfSales);

    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<PointOfSaleListDTO> getPointOfSalesList(String merchantId, String tokenMerchantId, String type,
      String city, String address, String contactName, Pageable pageable) {
    String sanitizedMerchantId = sanitizeString(merchantId);
    log.info("[POINT-OF-SALE][GET] Fetching points of sale for merchantId={}", sanitizedMerchantId);

    validateMerchantAccess(tokenMerchantId, sanitizedMerchantId);

    Page<PointOfSale> pagePointOfSales = getPointOfSaleService.getPointOfSalesList(sanitizedMerchantId,
        type, city, address, contactName, pageable);

    return buildPointOfSalesListResponse(pagePointOfSales);
  }

  @Override
  public ResponseEntity<PointOfSaleListDTO> getPointOfSalesListByInitiative(
      String merchantId, String initiativeId, String tokenMerchantId, String type,
      String city, String address, String contactName, Pageable pageable) {
    String sanitizedMerchantId = sanitizeString(merchantId);
    String sanitizedInitiativeId = sanitizeString(initiativeId);

    log.info("[POINT-OF-SALE][GET] Fetching points of sale for merchantId={} and initiativeId={}",
        sanitizedMerchantId, sanitizedInitiativeId);

    validateMerchantAccess(tokenMerchantId, sanitizedMerchantId);

    Page<PointOfSale> pagePointOfSales = getPointOfSaleWithInitiativeService.getPointOfSalesListByInitiative(
        sanitizedInitiativeId, sanitizedMerchantId, type, city, address, contactName, pageable);

    return buildPointOfSalesListResponse(pagePointOfSales);
  }

  private ResponseEntity<PointOfSaleListDTO> buildPointOfSalesListResponse(
      Page<PointOfSale> pagePointOfSales) {

    Page<PointOfSaleDTO> result = pagePointOfSales.map(pointOfSaleDTOMapper::entityToDto);

    PointOfSaleListDTO pointOfSales = PointOfSaleListDTO.builder()
        .content(result.getContent()).pageNo(result.getNumber()).pageSize(result.getSize())
        .totalElements(result.getTotalElements()).totalPages(result.getTotalPages()).build();

    return ResponseEntity.ok(pointOfSales);
  }

  @Override
  public ResponseEntity<PointOfSaleDTO> getPointOfSale(String pointOfSaleId, String merchantId,
      String tokenPointOfSaleId, String tokenMerchantId) {

    String sanitizedPointOfSaleId = sanitizeString(pointOfSaleId);
    String sanitizedMerchantId = sanitizeString(merchantId);

    validatePointOfSaleAccess(tokenMerchantId, tokenPointOfSaleId, sanitizedMerchantId,
        sanitizedPointOfSaleId);

    PointOfSale pointOfSale = getPointOfSaleService.getPointOfSaleByIdAndMerchantId(
        sanitizedPointOfSaleId, sanitizedMerchantId);

    return buildPointOfSaleResponse(pointOfSale, sanitizedMerchantId);
  }

  @Override
  public ResponseEntity<PointOfSaleDTO> getPointOfSaleByInitiative(String pointOfSaleId,
      String merchantId, String initiativeId, String tokenPointOfSaleId, String tokenMerchantId) {

    String sanitizedInitiativeId = sanitizeString(initiativeId);
    String sanitizedPointOfSaleId = sanitizeString(pointOfSaleId);
    String sanitizedMerchantId = sanitizeString(merchantId);

    validatePointOfSaleAccess(tokenMerchantId, tokenPointOfSaleId, sanitizedMerchantId,
        sanitizedPointOfSaleId);

    PointOfSale pointOfSale = getPointOfSaleWithInitiativeService.getPointOfSaleByIdAndMerchantIdAndInitiativeId(
        sanitizedInitiativeId, sanitizedPointOfSaleId, sanitizedMerchantId);

    return buildPointOfSaleResponse(pointOfSale, sanitizedMerchantId);
  }

  private void validatePointOfSaleAccess(String tokenMerchantId, String tokenPointOfSaleId,
      String merchantId, String pointOfSaleId) {
    log.info("[POINT-OF-SALE][GET] Fetching detail for pointOfSaleId={} for merchantId={}",
        pointOfSaleId, merchantId);

    validateMerchantAccess(tokenMerchantId, merchantId);

    if (tokenPointOfSaleId != null &&
        !Utilities.sanitizeString(tokenPointOfSaleId).equals(pointOfSaleId)) {

      throw new PointOfSaleNotAllowedException(
          "Point of sale mismatch: expected [%s], but received [%s]".formatted(tokenPointOfSaleId,
              pointOfSaleId)
      );
    }
  }

  private void validateMerchantAccess(String tokenMerchantId, String merchantId) {
    if (tokenMerchantId != null &&
        !Utilities.sanitizeString(tokenMerchantId).equals(merchantId)) {
      throw new MerchantNotAllowedException(
          MERCHANT_MISMATCH_MSG.formatted(tokenMerchantId, merchantId));
    }
  }

  private ResponseEntity<PointOfSaleDTO> buildPointOfSaleResponse(PointOfSale pointOfSale,
      String merchantId) {
    Merchant merchant = merchantService.getMerchantByMerchantId(merchantId);

    PointOfSaleDTO dto = pointOfSaleDTOMapper.entityToDto(pointOfSale, merchant);

    return ResponseEntity.ok(dto);
  }
}
