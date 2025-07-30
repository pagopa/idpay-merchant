package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDetailDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleListDTO;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.service.merchant.MerchantDetailService;
import it.gov.pagopa.merchant.service.pointofsales.PointOfSaleService;
import it.gov.pagopa.merchant.utils.validator.PointOfSaleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class PointOfSaleControllerImpl implements PointOfSaleController{

  private final PointOfSaleService pointOfSaleService;
  private final PointOfSaleValidator pointOfSaleValidator;
  private final PointOfSaleDTOMapper pointOfSaleDTOMapper;
  private final MerchantDetailService merchantDetailService;

  public PointOfSaleControllerImpl(PointOfSaleService pointOfSaleService,
                                   PointOfSaleValidator pointOfSaleValidator,
                                   PointOfSaleDTOMapper pointOfSaleDTOMapper,
                                   MerchantDetailService merchantDetailService) {
    this.pointOfSaleService = pointOfSaleService;
    this.pointOfSaleValidator = pointOfSaleValidator;
    this.pointOfSaleDTOMapper = pointOfSaleDTOMapper;
    this.merchantDetailService = merchantDetailService;
  }


  @Override
  public ResponseEntity<Void> savePointOfSales(String merchantId, List<PointOfSaleDTO> pointOfSales){
    pointOfSaleValidator.validatePointOfSales(pointOfSales);
    pointOfSaleValidator.validateViolationsPointOfSales(pointOfSales);

    log.info("[POINT-OF-SALES][SAVE] Saving {} point(s) of sale for merchantId={}",pointOfSales.size(),merchantId);

    List<PointOfSale> entities = pointOfSales.stream()
            .map(pointOfSaleDTO -> pointOfSaleDTOMapper.pointOfSaleDTOtoPointOfSaleEntity(pointOfSaleDTO,merchantId))
            .toList();

    pointOfSaleService.savePointOfSales(merchantId, entities);

    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<PointOfSaleListDTO> getPointOfSalesList(String merchantId, String type, String city, String address, String contactName, Pageable pageable) {

    log.info("[POINT-OF-SALE][GET] Fetching points of sale for merchantId={}",merchantId);

    Page<PointOfSale> pagePointOfSales = pointOfSaleService.getPointOfSalesList(
            merchantId,
            type,
            city,
            address,
            contactName,
            pageable);

    Page<PointOfSaleDTO> result = pagePointOfSales.map(pointOfSaleDTOMapper::pointOfSaleEntityToPointOfSaleDTO);

    PointOfSaleListDTO pointOfSAleListDTO = PointOfSaleListDTO.builder()
            .content(result.getContent())
            .pageNo(result.getNumber())
            .pageSize(result.getSize())
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .build();

    return ResponseEntity.ok(pointOfSAleListDTO);
  }

  @Override
  public ResponseEntity<PointOfSaleDetailDTO> getPointOfSale(String merchantId, String pointOfSaleId) {

    log.info("[POINT-OF-SALE][GET] Fetching detail for pointOfSaleId={} and merchantId={}", pointOfSaleId, merchantId);

    PointOfSale pointOfSale = pointOfSaleService.getPointOfSaleById(pointOfSaleId);
    MerchantDetailDTO merchantDetail = merchantDetailService.getMerchantDetail(merchantId);

    PointOfSaleDetailDTO responseDTO = PointOfSaleDetailDTO.builder()
            .pointOfSale(pointOfSaleDTOMapper.pointOfSaleEntityToPointOfSaleDTO(pointOfSale))
            .merchantDetail(merchantDetail)
            .build();

    return ResponseEntity.ok(responseDTO);
  }
}



