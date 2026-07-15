package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleInitiativeListDTO;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleInitiativeFilter;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleNotFoundException;
import it.gov.pagopa.merchant.mapper.PointOfSaleInitiativeDTOMapper;
import it.gov.pagopa.merchant.model.Initiative;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.model.PointOfSalesInitiative;
import it.gov.pagopa.merchant.repository.MerchantRepository;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.repository.PointOfSalesInitiativeRepository;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleFaker;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointOfSaleInitiativeFinderServiceTest {

  @Mock
  private MerchantService merchantServiceMock;
  @Mock
  private PointOfSaleRepository repositoryMock;
  @Mock
  private PointOfSalesInitiativeRepository pointOfSalesInitiativeRepositoryMock;
  @Mock
  private MerchantRepository merchantRepositoryMock;
  private static final String MERCHANT_ID = "MERCHANT_ID";
  private static final String INITIATIVE_ID = "INITIATIVE_ID";

  private PointOfSaleInitiativeFinderService service;

  @BeforeEach
  void setUp() {
    PointOfSaleInitiativeDTOMapper pointOfSaleInitiativeDTOMapper = new PointOfSaleInitiativeDTOMapper();

    service = new PointOfSaleInitiativeFinderServiceImpl(
            repositoryMock,
            pointOfSalesInitiativeRepositoryMock,
            merchantServiceMock,
            merchantRepositoryMock,
            pointOfSaleInitiativeDTOMapper);
  }


  @Test
  void getPointOfSalesListByInitiative_filtersByRelation() {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId("POS_ID");

    when(merchantServiceMock.getMerchantDetail(MERCHANT_ID))
        .thenReturn(MerchantDetailDTOFaker.mockInstance(1));
    when(pointOfSalesInitiativeRepositoryMock
        .findPointOfSaleIdsByInitiativeIdAndMerchantIdAndEnabledTrue(INITIATIVE_ID, MERCHANT_ID))
        .thenReturn(List.of("POS_ID"));

    Criteria criteria = new Criteria();
    when(repositoryMock.getCriteria(eq(MERCHANT_ID), eq(List.of("POS_ID")), any(), any(), any(),
        any())).thenReturn(criteria);
    when(repositoryMock.findByFilter(eq(criteria), any())).thenReturn(List.of(pointOfSale));

    Pageable paging = PageRequest.of(0, 20, Sort.by("franchiseName"));
    Page<PointOfSale> pointOfSalePage = service.getPointOfSalesListByInitiative(
        INITIATIVE_ID, MERCHANT_ID, null, null, null, null, paging);

    assertNotNull(pointOfSalePage);
    Assertions.assertEquals(1, pointOfSalePage.getTotalElements());
    verify(pointOfSalesInitiativeRepositoryMock)
        .findPointOfSaleIdsByInitiativeIdAndMerchantIdAndEnabledTrue(INITIATIVE_ID, MERCHANT_ID);
    verify(repositoryMock).getCriteria(eq(MERCHANT_ID), eq(List.of("POS_ID")), any(), any(), any(),
        any());
    verify(repositoryMock).getCount(criteria);
  }

  @Test
  void getPointOfSalesListByInitiative_withNoRelations_returnsEmptyPage() {
    when(merchantServiceMock.getMerchantDetail(MERCHANT_ID))
        .thenReturn(MerchantDetailDTOFaker.mockInstance(1));
    when(pointOfSalesInitiativeRepositoryMock
        .findPointOfSaleIdsByInitiativeIdAndMerchantIdAndEnabledTrue(INITIATIVE_ID, MERCHANT_ID))
        .thenReturn(List.of());

    Pageable paging = PageRequest.of(0, 20, Sort.by("franchiseName"));
    Page<PointOfSale> pointOfSalePage = service.getPointOfSalesListByInitiative(
        INITIATIVE_ID, MERCHANT_ID, null, null, null, null, paging);

    Assertions.assertTrue(pointOfSalePage.isEmpty());
    verify(repositoryMock, never()).findByFilter(any(), any());
  }

  @Test
  void getPointOfSalesListByInitiativeFilter_allInitiatives_filtersByAssociatedIds() {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId("POS_ID");

    when(merchantServiceMock.getMerchantDetail(MERCHANT_ID))
        .thenReturn(MerchantDetailDTOFaker.mockInstance(1));
    when(pointOfSalesInitiativeRepositoryMock.findPointOfSaleIdsByMerchantIdAndEnabledTrue(
        MERCHANT_ID))
        .thenReturn(List.of("POS_ID"));

    Criteria criteria = new Criteria();
    when(repositoryMock.getCriteria(eq(MERCHANT_ID), eq(List.of("POS_ID")), any(), any(), any(),
        any())).thenReturn(criteria);
    when(repositoryMock.findByFilter(eq(criteria), any())).thenReturn(List.of(pointOfSale));

    Pageable paging = PageRequest.of(0, 20, Sort.by("franchiseName"));
    Page<PointOfSale> pointOfSalePage = service.getPointOfSalesListByInitiativeFilter(
        PointOfSaleInitiativeFilter.ALL_INITIATIVES, MERCHANT_ID, null, null, null, null, paging);

    assertNotNull(pointOfSalePage);
    Assertions.assertEquals(1, pointOfSalePage.getTotalElements());
    verify(repositoryMock).getCriteria(eq(MERCHANT_ID), eq(List.of("POS_ID")), any(), any(), any(),
        any());
    verify(repositoryMock).getCount(criteria);
  }

  @Test
  void getPointOfSalesListByInitiativeFilter_allInitiativesWithNoRelations_returnsEmptyPage() {
    when(merchantServiceMock.getMerchantDetail(MERCHANT_ID))
        .thenReturn(MerchantDetailDTOFaker.mockInstance(1));
    when(pointOfSalesInitiativeRepositoryMock.findPointOfSaleIdsByMerchantIdAndEnabledTrue(
        MERCHANT_ID))
        .thenReturn(List.of());

    Pageable paging = PageRequest.of(0, 20, Sort.by("franchiseName"));
    Page<PointOfSale> pointOfSalePage = service.getPointOfSalesListByInitiativeFilter(
        PointOfSaleInitiativeFilter.ALL_INITIATIVES, MERCHANT_ID, null, null, null, null, paging);

    Assertions.assertTrue(pointOfSalePage.isEmpty());
    verify(repositoryMock, never()).findByFilter(any(), any());
    verify(repositoryMock, never()).getCount(any());
  }

  @Test
  void getPointOfSalesListByInitiativeFilter_noInitiative_excludesAssociatedIds() {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId("POS_ID");

    when(merchantServiceMock.getMerchantDetail(MERCHANT_ID))
        .thenReturn(MerchantDetailDTOFaker.mockInstance(1));
    when(pointOfSalesInitiativeRepositoryMock.findPointOfSaleIdsByMerchantIdAndEnabledTrue(
        MERCHANT_ID))
        .thenReturn(List.of("ASSOCIATED_POS_ID"));

    Criteria criteria = new Criteria();
    when(repositoryMock.getCriteriaExcludingPointOfSaleIds(eq(MERCHANT_ID),
        eq(List.of("ASSOCIATED_POS_ID")), any(), any(), any(), any())).thenReturn(criteria);
    when(repositoryMock.findByFilter(eq(criteria), any())).thenReturn(List.of(pointOfSale));

    Pageable paging = PageRequest.of(0, 20, Sort.by("franchiseName"));
    Page<PointOfSale> pointOfSalePage = service.getPointOfSalesListByInitiativeFilter(
        PointOfSaleInitiativeFilter.NO_INITIATIVE, MERCHANT_ID, null, null, null, null, paging);

    assertNotNull(pointOfSalePage);
    Assertions.assertEquals(1, pointOfSalePage.getTotalElements());
    verify(repositoryMock).getCriteriaExcludingPointOfSaleIds(eq(MERCHANT_ID),
        eq(List.of("ASSOCIATED_POS_ID")), any(), any(), any(), any());
    verify(repositoryMock).getCount(criteria);
  }

  @Test
  void getPointOfSalesListByInitiativeFilter_noInitiativeWithNoRelations_usesPagedMerchantCriteria() {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId("POS_ID");

    when(merchantServiceMock.getMerchantDetail(MERCHANT_ID))
        .thenReturn(MerchantDetailDTOFaker.mockInstance(1));
    when(pointOfSalesInitiativeRepositoryMock.findPointOfSaleIdsByMerchantIdAndEnabledTrue(
        MERCHANT_ID))
        .thenReturn(List.of());

    Criteria criteria = new Criteria();
    when(repositoryMock.getCriteriaExcludingPointOfSaleIds(eq(MERCHANT_ID), eq(List.of()), any(),
        any(), any(), any())).thenReturn(criteria);
    when(repositoryMock.findByFilter(eq(criteria), any())).thenReturn(List.of(pointOfSale));

    Pageable paging = PageRequest.of(2, 8, Sort.by("franchiseName"));
    Page<PointOfSale> pointOfSalePage = service.getPointOfSalesListByInitiativeFilter(
        PointOfSaleInitiativeFilter.NO_INITIATIVE, MERCHANT_ID, null, null, null, null, paging);

    assertNotNull(pointOfSalePage);
    Assertions.assertEquals(2, pointOfSalePage.getNumber());
    Assertions.assertEquals(8, pointOfSalePage.getSize());
    verify(repositoryMock).findByFilter(criteria, paging);
    verify(repositoryMock).getCount(criteria);
  }

  @Test
  void getPointOfSaleByIdAndMerchantIdAndInitiativeIdOK() {
    String merchantId = "mock-merchant-id";
    String pointOfSaleId = new ObjectId().toHexString();
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId(pointOfSaleId);

    when(merchantServiceMock.getMerchantDetail(merchantId))
        .thenReturn(MerchantDetailDTOFaker.mockInstance(1));
    when(pointOfSalesInitiativeRepositoryMock.findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue(
        pointOfSaleId, INITIATIVE_ID, merchantId))
        .thenReturn(Optional.of(PointOfSalesInitiative.builder().build()));
    when(repositoryMock.findByIdAndMerchantId(pointOfSaleId, merchantId))
        .thenReturn(Optional.of(pointOfSale));

    PointOfSale result = service.getPointOfSaleByIdAndMerchantIdAndInitiativeId(
        INITIATIVE_ID, pointOfSaleId, merchantId);

    Assertions.assertEquals(pointOfSale, result);
  }

  @Test
  void getPointOfSaleByIdAndMerchantIdAndInitiativeId_withNoRelation_throwsNotFound() {
    String merchantId = "mock-merchant-id";
    String pointOfSaleId = new ObjectId().toHexString();

    when(merchantServiceMock.getMerchantDetail(merchantId))
        .thenReturn(MerchantDetailDTOFaker.mockInstance(1));
    when(pointOfSalesInitiativeRepositoryMock.findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue(
        pointOfSaleId, INITIATIVE_ID, merchantId))
        .thenReturn(Optional.empty());

    assertThrows(PointOfSaleNotFoundException.class,
        () -> service.getPointOfSaleByIdAndMerchantIdAndInitiativeId(
            INITIATIVE_ID, pointOfSaleId, merchantId));
    verify(repositoryMock, never()).findByIdAndMerchantId(pointOfSaleId, merchantId);
  }

  @Test
  void getPointOfSaleByIdAndMerchantIdAndInitiativeId_withMissingPointOfSale_throwsNotFound() {
    String merchantId = "mock-merchant-id";
    String pointOfSaleId = new ObjectId().toHexString();

    when(merchantServiceMock.getMerchantDetail(merchantId))
        .thenReturn(MerchantDetailDTOFaker.mockInstance(1));
    when(pointOfSalesInitiativeRepositoryMock.findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue(
        pointOfSaleId, INITIATIVE_ID, merchantId))
        .thenReturn(Optional.of(PointOfSalesInitiative.builder().build()));
    when(repositoryMock.findByIdAndMerchantId(pointOfSaleId, merchantId))
        .thenReturn(Optional.empty());

    assertThrows(PointOfSaleNotFoundException.class,
        () -> service.getPointOfSaleByIdAndMerchantIdAndInitiativeId(
            INITIATIVE_ID, pointOfSaleId, merchantId));
  }

  @Test
  void getPointOfSalesListByInitiative_merchantNotFound() {
    when(merchantServiceMock.getMerchantDetail(MERCHANT_ID)).thenReturn(null);
    Pageable pageable = PageRequest.of(0, 8);

    assertThrows(MerchantNotFoundException.class,
        () -> service.getPointOfSalesListByInitiative(
            INITIATIVE_ID, MERCHANT_ID, null, null, null, null, pageable));

    verifyNoInteractions(pointOfSalesInitiativeRepositoryMock);
  }

  @Test
  void getPointOfSaleByInitiative_merchantNotFound() {
    when(merchantServiceMock.getMerchantDetail(MERCHANT_ID)).thenReturn(null);

    assertThrows(MerchantNotFoundException.class,
        () -> service.getPointOfSaleByIdAndMerchantIdAndInitiativeId(
            INITIATIVE_ID, "POS_ID", MERCHANT_ID));

    verifyNoInteractions(pointOfSalesInitiativeRepositoryMock);
  }

  @Test
  void getInitiativesByPointOfSaleIdAndMerchantIdOK() {
    Instant createdAt = Instant.parse("2026-06-26T10:15:30Z");
    Instant updatedAt = Instant.parse("2026-06-26T11:15:30Z");
    PointOfSalesInitiative relation = PointOfSalesInitiative.builder()
        .pointOfSaleId("POS_ID")
        .initiativeId(INITIATIVE_ID)
        .merchantId(MERCHANT_ID)
        .enabled(true)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .build();

    when(merchantServiceMock.getMerchantDetail(MERCHANT_ID))
        .thenReturn(MerchantDetailDTOFaker.mockInstance(1));
    when(pointOfSalesInitiativeRepositoryMock.findInitiativesByPointOfSaleIdAndMerchantIdAndEnabledTrue(
        "POS_ID", MERCHANT_ID))
        .thenReturn(List.of(relation));

    PointOfSaleInitiativeListDTO result = service.getInitiativesByPointOfSaleIdAndMerchantId(
        "POS_ID", MERCHANT_ID);

    Assertions.assertEquals(1, result.getInitiatives().size());
    Assertions.assertEquals(INITIATIVE_ID, result.getInitiatives().get(0).getInitiativeId());
    Assertions.assertEquals(createdAt, result.getInitiatives().get(0).getCreatedAt());
    Assertions.assertEquals(updatedAt, result.getInitiatives().get(0).getUpdatedAt());
    verify(pointOfSalesInitiativeRepositoryMock)
        .findInitiativesByPointOfSaleIdAndMerchantIdAndEnabledTrue("POS_ID", MERCHANT_ID);
  }

  @Test
  void getInitiativesByPointOfSaleIdAndMerchantId_merchantNotFound() {
    when(merchantServiceMock.getMerchantDetail(MERCHANT_ID)).thenReturn(null);

    assertThrows(MerchantNotFoundException.class,
        () -> service.getInitiativesByPointOfSaleIdAndMerchantId("POS_ID", MERCHANT_ID));

    verifyNoInteractions(pointOfSalesInitiativeRepositoryMock);
  }

  @Test
  void getInitiativesByPointOfSaleId_OK() {

    String posId = "POS_ID";
    Instant expectedCreatedAt = Instant.parse("2026-07-15T10:15:30.00Z");

    Initiative initiative = Initiative.builder()
            .initiativeId(INITIATIVE_ID)
            .initiativeName("Test initiative")
            .organizationName("Org")
            .status("ACTIVE")
            .build();

    Merchant merchant = Merchant.builder()
            .merchantId(MERCHANT_ID)
            .initiativeList(List.of(initiative))
            .build();

    PointOfSalesInitiative relation = PointOfSalesInitiative.builder()
            .pointOfSaleId(posId)
            .initiativeId(INITIATIVE_ID)
            .createdAt(expectedCreatedAt)
            .build();

    when(merchantRepositoryMock.findById(MERCHANT_ID))
            .thenReturn(Optional.of(merchant));

    when(pointOfSalesInitiativeRepositoryMock.findByPointOfSaleIdAndEnabledTrue(posId))
            .thenReturn(List.of(relation));


    PointOfSaleInitiativeListDTO result =
            service.getInitiativesByPointOfSaleId(posId, MERCHANT_ID);


    Assertions.assertNotNull(result);
    Assertions.assertEquals(1, result.getInitiatives().size());
    Assertions.assertEquals(INITIATIVE_ID, result.getInitiatives().get(0).getInitiativeId());
    Assertions.assertEquals(expectedCreatedAt, result.getInitiatives().get(0).getCreatedAt());

    verify(merchantRepositoryMock).findById(MERCHANT_ID);
    verify(pointOfSalesInitiativeRepositoryMock).findByPointOfSaleIdAndEnabledTrue(posId);
  }

  @Test
  void getInitiativesByPointOfSaleId_withNullRelation_shouldMapNullCreatedAt() {
    String posId = "POS_ID";

    Initiative initiative = Initiative.builder()
            .initiativeId(INITIATIVE_ID)
            .initiativeName("Test initiative")
            .organizationName("Org")
            .status("ACTIVE")
            .build();

    Merchant merchant = Merchant.builder()
            .merchantId(MERCHANT_ID)
            .initiativeList(List.of(initiative))
            .build();

    PointOfSalesInitiative relation = PointOfSalesInitiative.builder()
            .pointOfSaleId(posId)
            .initiativeId("ANOTHER_INITIATIVE_ID")
            .build();

    when(merchantRepositoryMock.findById(MERCHANT_ID))
            .thenReturn(Optional.of(merchant));

    when(pointOfSalesInitiativeRepositoryMock.findByPointOfSaleIdAndEnabledTrue(posId))
            .thenReturn(List.of(relation));

    PointOfSaleInitiativeListDTO result =
            service.getInitiativesByPointOfSaleId(posId, MERCHANT_ID);

    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.getInitiatives().isEmpty());
  }

  @Test
  void getInitiativesByPointOfSaleId_withDuplicateRelations_shouldMergeWithoutThrowingException() {
    String posId = "POS_ID";
    Instant firstCreatedAt = Instant.parse("2026-07-15T10:15:30.00Z");
    Instant secondCreatedAt = Instant.parse("2026-07-15T11:15:30.00Z");

    Initiative initiative = Initiative.builder()
            .initiativeId(INITIATIVE_ID)
            .initiativeName("Test initiative")
            .organizationName("Org")
            .status("ACTIVE")
            .build();

    Merchant merchant = Merchant.builder()
            .merchantId(MERCHANT_ID)
            .initiativeList(List.of(initiative))
            .build();

    PointOfSalesInitiative relation1 = PointOfSalesInitiative.builder()
            .pointOfSaleId(posId)
            .initiativeId(INITIATIVE_ID)
            .createdAt(firstCreatedAt)
            .build();

    PointOfSalesInitiative relation2 = PointOfSalesInitiative.builder()
            .pointOfSaleId(posId)
            .initiativeId(INITIATIVE_ID)
            .createdAt(secondCreatedAt)
            .build();

    when(merchantRepositoryMock.findById(MERCHANT_ID))
            .thenReturn(Optional.of(merchant));

    when(pointOfSalesInitiativeRepositoryMock.findByPointOfSaleIdAndEnabledTrue(posId))
            .thenReturn(List.of(relation1, relation2));

    PointOfSaleInitiativeListDTO result =
            service.getInitiativesByPointOfSaleId(posId, MERCHANT_ID);

    Assertions.assertNotNull(result);

    Assertions.assertEquals(1, result.getInitiatives().size());

    Assertions.assertEquals(firstCreatedAt, result.getInitiatives().get(0).getCreatedAt());

    verify(merchantRepositoryMock).findById(MERCHANT_ID);
    verify(pointOfSalesInitiativeRepositoryMock).findByPointOfSaleIdAndEnabledTrue(posId);
  }

  @Test
  void getInitiativesByPointOfSaleId_filtersOutInvalidInitiatives() {

    String posId = "POS_ID";

    Merchant merchant = Merchant.builder()
            .merchantId(MERCHANT_ID)
            .initiativeList(List.of())
            .build();

    PointOfSalesInitiative relation = PointOfSalesInitiative.builder()
            .pointOfSaleId(posId)
            .initiativeId("NOT_EXISTING")
            .build();

    when(merchantRepositoryMock.findById(MERCHANT_ID))
            .thenReturn(Optional.of(merchant));

    when(pointOfSalesInitiativeRepositoryMock.findByPointOfSaleIdAndEnabledTrue(posId))
            .thenReturn(List.of(relation));


    PointOfSaleInitiativeListDTO result =
            service.getInitiativesByPointOfSaleId(posId, MERCHANT_ID);


    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.getInitiatives().isEmpty());
  }

  @Test
  void getInitiativesByPointOfSaleId_noRelations_returnsEmptyList() {

    Merchant merchant = Merchant.builder()
            .merchantId(MERCHANT_ID)
            .initiativeList(List.of())
            .build();

    when(merchantRepositoryMock.findById(MERCHANT_ID))
            .thenReturn(Optional.of(merchant));

    when(pointOfSalesInitiativeRepositoryMock.findByPointOfSaleIdAndEnabledTrue("POS_ID"))
            .thenReturn(List.of());

    PointOfSaleInitiativeListDTO result =
            service.getInitiativesByPointOfSaleId("POS_ID", MERCHANT_ID);

    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.getInitiatives().isEmpty());
  }

  @Test
  void getInitiativesByPointOfSaleId_merchantNotFound() {
    when(merchantRepositoryMock.findById(MERCHANT_ID))
            .thenReturn(Optional.empty());

    assertThrows(MerchantNotFoundException.class,
            () -> service.getInitiativesByPointOfSaleId("POS_ID", MERCHANT_ID));

    verifyNoInteractions(pointOfSalesInitiativeRepositoryMock);
  }
}
