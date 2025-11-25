package it.gov.pagopa.merchant.controller;

import static it.gov.pagopa.merchant.utils.Utilities.sanitizeString;

import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleListDTO;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.service.pointofsales.PointOfSaleService;
import it.gov.pagopa.merchant.utils.validator.PointOfSaleValidator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class PointOfSaleControllerImpl implements PointOfSaleController {

  private final PointOfSaleService pointOfSaleService;
  private final PointOfSaleValidator pointOfSaleValidator;
  private final PointOfSaleDTOMapper pointOfSaleDTOMapper;
  private final MerchantService merchantService;

  public PointOfSaleControllerImpl(PointOfSaleService pointOfSaleService,
      PointOfSaleValidator pointOfSaleValidator,
      PointOfSaleDTOMapper pointOfSaleDTOMapper, MerchantService merchantService) {
    this.pointOfSaleService = pointOfSaleService;
    this.pointOfSaleValidator = pointOfSaleValidator;
    this.pointOfSaleDTOMapper = pointOfSaleDTOMapper;
    this.merchantService = merchantService;
  }


  @Override
  public ResponseEntity<Void> savePointOfSales(String merchantId,
      List<PointOfSaleDTO> pointOfSales) {
    pointOfSaleValidator.validatePointOfSales(pointOfSales);
    pointOfSaleValidator.validateViolationsPointOfSales(pointOfSales);

    String sanitizedMerchantId = sanitizeString(merchantId);
    log.info("[POINT-OF-SALES][SAVE] Saving {} point(s) of sale for merchantId={}",
        pointOfSales.size(), sanitizedMerchantId);

    List<PointOfSale> entities = pointOfSales.stream()
        .map(pointOfSaleDTO -> pointOfSaleDTOMapper.dtoToEntity(pointOfSaleDTO, sanitizedMerchantId))
        .toList();

    pointOfSaleService.savePointOfSales(sanitizedMerchantId, entities);

    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<PointOfSaleListDTO> getPointOfSalesList(String merchantId, String type,
      String city, String address, String contactName, Pageable pageable) {
    String sanitizedMerchantId = sanitizeString(merchantId);
    log.info("[POINT-OF-SALE][GET] Fetching points of sale for merchantId={}", sanitizedMerchantId);

    Page<PointOfSale> pagePointOfSales = pointOfSaleService.getPointOfSalesList(sanitizedMerchantId, type,
        city, address, contactName, pageable);

    Page<PointOfSaleDTO> result = pagePointOfSales.map(pointOfSaleDTOMapper::entityToDto);

    PointOfSaleListDTO pointOfSales = PointOfSaleListDTO.builder()
        .content(result.getContent()).pageNo(result.getNumber()).pageSize(result.getSize())
        .totalElements(result.getTotalElements()).totalPages(result.getTotalPages()).build();

    return ResponseEntity.ok(pointOfSales);
  }

  @Override
  public ResponseEntity<PointOfSaleDTO> getPointOfSale(String pointOfSaleId, String merchantId) {
    String sanitizedPointOfSaleId = sanitizeString(pointOfSaleId);
    String sanitizedMerchantId = sanitizeString(merchantId);
    log.info("[POINT-OF-SALE][GET] Fetching detail for pointOfSaleId={} for merchantId={}",
        sanitizedPointOfSaleId, sanitizedMerchantId);

    PointOfSale pointOfSale = pointOfSaleService.getPointOfSaleByIdAndMerchantId(sanitizedPointOfSaleId,
        sanitizedMerchantId);

    Merchant merchant = merchantService.getMerchantByMerchantId(sanitizedMerchantId);

    PointOfSaleDTO dto = pointOfSaleDTOMapper.entityToDto(pointOfSale, merchant);

    return ResponseEntity.ok(dto);
  }
}