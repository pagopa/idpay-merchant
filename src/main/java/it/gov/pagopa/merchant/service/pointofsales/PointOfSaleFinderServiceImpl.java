package it.gov.pagopa.merchant.service.pointofsales;


import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PointOfSaleFinderServiceImpl implements PointOfSaleFinderService {

  private final PointOfSaleRepository pointOfSaleRepository;
  private final PointOfSaleInitiativeFinderServiceImpl getPointOfSaleWithInitiativeService;

  public PointOfSaleFinderServiceImpl(
          PointOfSaleRepository pointOfSaleRepository,
          PointOfSaleInitiativeFinderServiceImpl getPointOfSaleWithInitiativeService) {
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


  @Override
  public PointOfSale getPointOfSaleByIdAndMerchantId(String pointOfSaleId, String merchantId) {
    getPointOfSaleWithInitiativeService.verifyMerchantExists(merchantId);

    return getPointOfSaleWithInitiativeService.findPointOfSaleByIdAndMerchantId(pointOfSaleId, merchantId);
  }

}
