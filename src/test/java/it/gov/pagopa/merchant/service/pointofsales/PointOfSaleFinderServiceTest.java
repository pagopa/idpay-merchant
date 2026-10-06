package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.constants.PointOfSaleConstants;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleNotFoundException;
import it.gov.pagopa.merchant.mapper.PointOfSaleInitiativeDTOMapper;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.repository.PointOfSalesInitiativeRepository;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleFaker;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointOfSaleFinderServiceTest {

  @Mock
  private MerchantService merchantServiceMock;
  @Mock
  private PointOfSaleRepository repositoryMock;
  @Mock
  private PointOfSalesInitiativeRepository pointOfSalesInitiativeRepositoryMock;
  @Mock
  private MerchantRepository merchantRepositoryMock;

  private PointOfSaleFinderService service;

  private static final String MERCHANT_ID = "MERCHANT_ID";


  @BeforeEach
  void setUp() {
    PointOfSaleInitiativeFinderServiceImpl getPointOfSaleWithInitiativeService =
        new PointOfSaleInitiativeFinderServiceImpl(
            repositoryMock,
            pointOfSalesInitiativeRepositoryMock,
            merchantServiceMock,
            merchantRepositoryMock,
            new PointOfSaleInitiativeDTOMapper()
        );

    service = new PointOfSaleFinderServiceImpl(
        repositoryMock,
        getPointOfSaleWithInitiativeService);
  }

  @Test
  void getPointOfSalesListOK() {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);

    Criteria criteria = new Criteria();
    when(repositoryMock.getCriteria(any(), any(), any(), any(), any())).thenReturn(criteria);
    when(repositoryMock.findByFilter(any(), any())).thenReturn(List.of(pointOfSale));

    Pageable paging = PageRequest.of(0, 20, Sort.by("franchiseName"));
    Page<PointOfSale> pointOfSalePage = service.getPointOfSalesList(MERCHANT_ID, "type",
            "city", "address", "contactName", paging);
    assertNotNull(pointOfSalePage);
  }

  @Test
  void getPointOfSaleByIdAndMerchantIdOK() {
    String merchantId = "mock-merchant-id";
    String pointOfSaleId = new ObjectId().toHexString();

    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId(pointOfSaleId);

    when(merchantServiceMock.getMerchantDetail(merchantId))
        .thenReturn(MerchantDetailDTOFaker.mockInstance(1));
    when(repositoryMock.findByIdAndMerchantId(String.valueOf(new ObjectId(pointOfSaleId)),
        merchantId))
        .thenReturn(Optional.of(pointOfSale));

    PointOfSale result = service.getPointOfSaleByIdAndMerchantId(pointOfSaleId, merchantId);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(pointOfSale, result);
    verify(repositoryMock).findByIdAndMerchantId(String.valueOf(new ObjectId(pointOfSaleId)),
        merchantId);
  }

  @Test
  void getPointOfSaleByIdAndMerchantId_KO_notFound() {
    String merchantId = "mock-merchant-id";
    ObjectId fakeId = new ObjectId();
    String pointOfSaleId = fakeId.toHexString();

    when(merchantServiceMock.getMerchantDetail(merchantId))
        .thenReturn(MerchantDetailDTOFaker.mockInstance(1));
    when(repositoryMock.findByIdAndMerchantId(String.valueOf(fakeId), merchantId))
        .thenReturn(Optional.empty());

    PointOfSaleNotFoundException ex = Assertions.assertThrows(PointOfSaleNotFoundException.class,
        () -> service.getPointOfSaleByIdAndMerchantId(pointOfSaleId, merchantId));

    Assertions.assertEquals(
        String.format(PointOfSaleConstants.MSG_NOT_FOUND, pointOfSaleId),
        ex.getMessage()
    );

    verify(repositoryMock).findByIdAndMerchantId(String.valueOf(fakeId), merchantId);
  }

}
