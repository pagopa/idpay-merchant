package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.constants.PointOfSaleConstants;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleNotFoundException;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GetPointOfSaleServiceImpl implements GetPointOfSaleService {

  private final PointOfSaleRepository pointOfSaleRepository;
  private final GetPointOfSaleWithInitiativeServiceImpl getPointOfSaleWithInitiativeService;

  public GetPointOfSaleServiceImpl(
          PointOfSaleRepository pointOfSaleRepository,
          GetPointOfSaleWithInitiativeServiceImpl getPointOfSaleWithInitiativeService) {
    this.pointOfSaleRepository = pointOfSaleRepository;
    this.getPointOfSaleWithInitiativeService = getPointOfSaleWithInitiativeService;
  }

  @Override
  public Page<PointOfSale> getPointOfSalesList(
          String merchantId,
          String type,
          String city,
          String address,
          String contactName,
          Pageable pageable) {

    getPointOfSaleWithInitiativeService.verifyMerchantExists(merchantId);

    Criteria criteria = pointOfSaleRepository.getCriteria(merchantId, type, city, address,
            contactName);
    return getPointOfSaleWithInitiativeService.getPointOfSalesPage(criteria, pageable);
  }

  public PointOfSale getPointOfSaleById(String pointOfSaleId) {
    return pointOfSaleRepository.findById(pointOfSaleId)
            .orElseThrow(() -> new PointOfSaleNotFoundException(
                    String.format(PointOfSaleConstants.MSG_NOT_FOUND, pointOfSaleId)));
  }

  @Override
  public PointOfSale getPointOfSaleByIdAndMerchantId(String pointOfSaleId, String merchantId) {
    getPointOfSaleWithInitiativeService.verifyMerchantExists(merchantId);

    return getPointOfSaleWithInitiativeService.findPointOfSaleByIdAndMerchantId(pointOfSaleId, merchantId);
  }

}
