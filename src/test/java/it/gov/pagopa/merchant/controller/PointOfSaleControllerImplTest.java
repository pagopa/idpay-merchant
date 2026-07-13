package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.common.config.JsonConfig;
import it.gov.pagopa.merchant.configuration.ServiceExceptionConfig;
import it.gov.pagopa.merchant.dto.enums.PosOnbordingRejectionReason;
import it.gov.pagopa.merchant.dto.pointofsales.*;
import it.gov.pagopa.merchant.exception.custom.MerchantNotAllowedException;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleNotAllowedException;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleNotFoundException;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.service.merchant.MerchantDetailService;
import it.gov.pagopa.merchant.service.pointofsales.PointOfSaleFinderService;
import it.gov.pagopa.merchant.service.pointofsales.PointOfSaleInitiativeFinderService;
import it.gov.pagopa.merchant.service.pointofsales.PointOfSaleWriter;
import it.gov.pagopa.merchant.service.pointofsales.UpdatePointOfSaleReferentService;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleDTOFaker;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleFaker;
import it.gov.pagopa.merchant.utils.validator.PointOfSaleValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static it.gov.pagopa.merchant.constants.PointOfSaleConstants.MSG_NOT_FOUND;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value={PointOfSaleControllerImpl.class}, excludeAutoConfiguration = { UserDetailsServiceAutoConfiguration.class , SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({JsonConfig.class, ServiceExceptionConfig.class})
class PointOfSaleControllerImplTest {

    @MockitoBean
    private PointOfSaleFinderService pointOfSaleFinderService;
    @MockitoBean
    private PointOfSaleInitiativeFinderService pointOfSaleInitiativeFinderService;
    @MockitoBean
    private MerchantDetailService merchantDetailService;
    @MockitoBean
    private PointOfSaleValidator validator;
    @MockitoBean
    private PointOfSaleDTOMapper mapper;
    @MockitoBean
    private MerchantService merchantService;
    @MockitoBean
    private PointOfSaleWriter pointOfSaleWriterMock;
    @MockitoBean
    private UpdatePointOfSaleReferentService updatePointOfSaleReferentService;

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  private static final String BASE_URL = "/idpay/merchant/portal";
  private static final String GET_POINT_OF_SALES = "/%s/point-of-sales";
  private static final String INITIATIVE_ID = "INITIATIVE_ID";

  private static final String MERCHANT_ID = "MERCHANT_ID";

  @Test
  void savePointOfSalesOK() throws Exception {
    PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();

    doNothing().when(validator).validatePointOfSales(any());

    doNothing().when(pointOfSaleWriterMock).savePointOfSales(MERCHANT_ID, INITIATIVE_ID, List.of(pointOfSaleDTO));

    mockMvc.perform(
            MockMvcRequestBuilders.post(BASE_URL + "/" + MERCHANT_ID + "/initiatives/" +  INITIATIVE_ID + "/point-of-sales")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(List.of(pointOfSaleDTO)))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful())
        .andDo(print())
        .andReturn();
  }

  @Test
  void savePointOfSalesWithMatchingMerchantHeader_shouldReturnNoContent() throws Exception {
    PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();
    List<PointOfSaleDTO> pointOfSaleDTOList = List.of(pointOfSaleDTO);
    mockMvc.perform(
            MockMvcRequestBuilders.post(BASE_URL + "/" + MERCHANT_ID + "/initiatives/" +  INITIATIVE_ID + "/point-of-sales")
                .header("x-merchant-id", MERCHANT_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(pointOfSaleDTOList)))
        .andExpect(status().isNoContent());

    verify(pointOfSaleWriterMock).savePointOfSales(anyString(), anyString(), anyList());
  }

  @Test
  void getPointOfSalesListOK() throws Exception {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    PageRequest pageRequest = PageRequest.of(0, 10);
    Page<PointOfSale> expectedPage = new PageImpl<>(List.of(pointOfSale), pageRequest, 1);

    when(pointOfSaleFinderService.getPointOfSalesList(any(), any(), any(), any(), any(),
            any())).thenReturn(expectedPage);

    MvcResult result =
        mockMvc.perform(
                MockMvcRequestBuilders.get(BASE_URL + String.format(GET_POINT_OF_SALES, MERCHANT_ID)))
            .andExpect(status().is2xxSuccessful())
            .andDo(print())
            .andReturn();
    Assertions.assertNotNull(result);
  }

  @Test
  void getPointOfSalesListWithMatchingMerchantHeader_shouldReturnOk() throws Exception {
    when(pointOfSaleFinderService.getPointOfSalesList(any(), any(), any(), any(), any(), any()))
        .thenReturn(Page.empty());

    mockMvc.perform(
            MockMvcRequestBuilders.get(BASE_URL + String.format(GET_POINT_OF_SALES, MERCHANT_ID))
                .header("x-merchant-id", MERCHANT_ID))
        .andExpect(status().isOk());

    verify(pointOfSaleFinderService).getPointOfSalesList(eq(MERCHANT_ID), any(), any(), any(), any(),
        any());
  }

  @Test
  void getPointOfSalesListWithInitiativeQueryParam_shouldUseInitiativeFilter() throws Exception {
    when(pointOfSaleInitiativeFinderService.getPointOfSalesListByInitiative(
        any(), any(), any(), any(), any(), any(), any())).thenReturn(Page.empty());

    mockMvc.perform(
            MockMvcRequestBuilders.get(BASE_URL + String.format(GET_POINT_OF_SALES, MERCHANT_ID))
                .param("initiativeId", INITIATIVE_ID)
                .param("type", "Fisico")
                .param("city", "Rieti")
                .param("address", "Via Nome")
                .param("contactName", "Mario Rossi")
                .param("page", "1")
                .param("size", "10")
                .param("sort", "city,asc"))
        .andExpect(status().isOk());

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(pointOfSaleInitiativeFinderService).getPointOfSalesListByInitiative(
        eq(INITIATIVE_ID), eq(MERCHANT_ID), eq("Fisico"), eq("Rieti"), eq("Via Nome"),
        eq("Mario Rossi"), pageableCaptor.capture());
    verifyNoInteractions(pointOfSaleFinderService);
    assertEquals(1, pageableCaptor.getValue().getPageNumber());
    assertEquals(10, pageableCaptor.getValue().getPageSize());
    Assertions.assertTrue(pageableCaptor.getValue().getSort().isSorted());
  }

  @Test
  void getPointOfSalesListByInitiativeOK() throws Exception {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    PageRequest pageRequest = PageRequest.of(0, 10);
    Page<PointOfSale> expectedPage = new PageImpl<>(List.of(pointOfSale), pageRequest, 1);

    when(pointOfSaleInitiativeFinderService.getPointOfSalesListByInitiative(eq(INITIATIVE_ID), eq(MERCHANT_ID),
        any(), any(), any(), any(), any())).thenReturn(expectedPage);

    mockMvc.perform(MockMvcRequestBuilders.get(
            BASE_URL + "/" + MERCHANT_ID + "/initiatives/" + INITIATIVE_ID + "/point-of-sales"))
        .andExpect(status().isOk());

    verify(pointOfSaleInitiativeFinderService).getPointOfSalesListByInitiative(eq(INITIATIVE_ID), eq(MERCHANT_ID),
        any(), any(), any(), any(), any());
  }

  @Test
  void getPointOfSalesListByInitiativeMerchantMismatch_shouldReturnForbidden() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get(
            BASE_URL + "/" + MERCHANT_ID + "/initiatives/" + INITIATIVE_ID + "/point-of-sales")
        .header("x-merchant-id", "DIFFERENT_MERCHANT_ID"))
        .andExpect(status().isForbidden());

    verifyNoInteractions(pointOfSaleFinderService, pointOfSaleInitiativeFinderService);
  }

  @Test
  void getPointOfSaleInitiativesOK() throws Exception {
    Instant createdAt = Instant.parse("2026-06-26T10:15:30Z");
    Instant updatedAt = Instant.parse("2026-06-26T11:15:30Z");
    when(pointOfSaleInitiativeFinderService.getInitiativesByPointOfSaleIdAndMerchantId(
        "POS_ID", MERCHANT_ID))
        .thenReturn(PointOfSaleInitiativeListDTO.builder()
            .initiatives(List.of(PointOfSaleInitiativeDTO.builder()
                .initiativeId(INITIATIVE_ID)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build()))
            .build());

    MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(
            BASE_URL + "/" + MERCHANT_ID + "/point-of-sales/POS_ID/initiatives"))
        .andExpect(status().isOk())
        .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    Assertions.assertTrue(responseBody.contains("\"initiatives\":["));
    Assertions.assertTrue(responseBody.contains("\"initiativeId\":\"INITIATIVE_ID\""));
    Assertions.assertTrue(responseBody.contains("\"createdAt\":\"2026-06-26T10:15:30Z\""));
    Assertions.assertTrue(responseBody.contains("\"updatedAt\":\"2026-06-26T11:15:30Z\""));
    verify(pointOfSaleInitiativeFinderService)
        .getInitiativesByPointOfSaleIdAndMerchantId("POS_ID", MERCHANT_ID);
  }

    @Test
    void getPointOfSaleTestOK() throws Exception {
        PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
        Merchant merchant = mock(Merchant.class);
        PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();

        when(pointOfSaleFinderService.getPointOfSaleByIdAndMerchantId(anyString(), anyString()))
                .thenReturn(pointOfSale);
        when(merchantService.getMerchantByMerchantId(anyString()))
            .thenReturn(merchant);
        when(mapper.entityToDto(pointOfSale, merchant)).thenReturn(pointOfSaleDTO);

    MvcResult result = mockMvc.perform(
            MockMvcRequestBuilders.get(BASE_URL + "/MERCHANT_ID/point-of-sales/POS_ID")
                .header("x-point-of-sale-id", "POS_ID")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(print())
        .andReturn();

    Assertions.assertNotNull(result);

        verify(pointOfSaleFinderService).getPointOfSaleByIdAndMerchantId(anyString(), anyString());
        verify(merchantService).getMerchantByMerchantId(MERCHANT_ID);
        verify(mapper).entityToDto(pointOfSale, merchant);
    }

  @Test
  void getPointOfSaleTestKO() throws Exception {
    String invalidPosId = "INVALID_POS_ID";

    when(pointOfSaleFinderService.getPointOfSaleByIdAndMerchantId(anyString(), anyString()))
        .thenThrow(new PointOfSaleNotFoundException(String.format(MSG_NOT_FOUND, invalidPosId)));

    mockMvc.perform(
            MockMvcRequestBuilders.get(BASE_URL + "/validMerchantId/point-of-sales/" + invalidPosId)
                .header("x-point-of-sale-id", invalidPosId)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(result -> Assertions.assertInstanceOf(PointOfSaleNotFoundException.class,
            result.getResolvedException()))
        .andExpect(result -> assertEquals(
            String.format(MSG_NOT_FOUND, invalidPosId),
            Objects.requireNonNull(result.getResolvedException()).getMessage()
        ))
        .andReturn();

    verify(pointOfSaleFinderService).getPointOfSaleByIdAndMerchantId(anyString(), anyString());
  }

  @Test
  void getPointOfSaleTestForbidden() throws Exception {
    String posId = "POS_ID";
    String tokenPosId = "DIFFERENT_POS_ID";

    mockMvc.perform(
            MockMvcRequestBuilders.get(BASE_URL + "/MERCHANT_ID/point-of-sales/" + posId)
                .header("x-point-of-sale-id", tokenPosId)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andExpect(result -> Assertions.assertInstanceOf(
            PointOfSaleNotAllowedException.class,
            result.getResolvedException()
        ))
        .andExpect(result -> Assertions.assertTrue(
            Objects.requireNonNull(result.getResolvedException()).getMessage()
                .contains("Point of sale mismatch")
        ))
        .andReturn();
  }

  @Test
  void testConstructorWithPrintStackTraceAndThrowable() {
    String message = "Test message";
    Throwable cause = new RuntimeException("Cause");

    PointOfSaleNotAllowedException ex = new PointOfSaleNotAllowedException(message, true, cause);

    assertNotNull(ex);
    assertEquals("POINT_OF_SALE_NOT_ALLOWED", ex.getCode());
    assertEquals(message, ex.getMessage());
    assertEquals(cause, ex.getCause());
  }

  @Test
  void getPointOfSaleWithNullHeaderPosId_shouldReturnOk() throws Exception {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    Merchant merchant = new Merchant();
    PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();

    when(pointOfSaleFinderService.getPointOfSaleByIdAndMerchantId(anyString(), anyString()))
        .thenReturn(pointOfSale);
    when(merchantService.getMerchantByMerchantId(anyString()))
        .thenReturn(merchant);
    when(mapper.entityToDto(any(PointOfSale.class), any(Merchant.class)))
        .thenReturn(pointOfSaleDTO);

    MvcResult result = mockMvc.perform(
            MockMvcRequestBuilders.get(BASE_URL + "/MERCHANT_ID/point-of-sales/POS_ID")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    Assertions.assertNotNull(result);

    verify(pointOfSaleFinderService).getPointOfSaleByIdAndMerchantId("POS_ID", "MERCHANT_ID");
    verify(merchantService).getMerchantByMerchantId("MERCHANT_ID");
    verify(mapper).entityToDto(pointOfSale, merchant);
  }

  @Test
  void getPointOfSaleByInitiativeOK() throws Exception {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    Merchant merchant = new Merchant();
    PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();

    when(pointOfSaleInitiativeFinderService.getPointOfSaleByIdAndMerchantIdAndInitiativeId(
        INITIATIVE_ID, "POS_ID", MERCHANT_ID)).thenReturn(pointOfSale);
    when(merchantService.getMerchantByMerchantId(MERCHANT_ID)).thenReturn(merchant);
    when(mapper.entityToDto(pointOfSale, merchant)).thenReturn(pointOfSaleDTO);

    mockMvc.perform(MockMvcRequestBuilders.get(
            BASE_URL + "/" + MERCHANT_ID + "/initiatives/" + INITIATIVE_ID
                + "/point-of-sales/POS_ID"))
        .andExpect(status().isOk());

    verify(pointOfSaleInitiativeFinderService).getPointOfSaleByIdAndMerchantIdAndInitiativeId(
        INITIATIVE_ID, "POS_ID", MERCHANT_ID);
  }

  @Test
  void getPointOfSaleByInitiativeNotFound() throws Exception {
    when(pointOfSaleInitiativeFinderService.getPointOfSaleByIdAndMerchantIdAndInitiativeId(
        INITIATIVE_ID, "POS_ID", MERCHANT_ID))
        .thenThrow(new PointOfSaleNotFoundException(String.format(MSG_NOT_FOUND, "POS_ID")));

    mockMvc.perform(MockMvcRequestBuilders.get(
            BASE_URL + "/" + MERCHANT_ID + "/initiatives/" + INITIATIVE_ID
                + "/point-of-sales/POS_ID"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getPointOfSaleByInitiativePointOfSaleMismatch_shouldReturnForbidden() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get(
            BASE_URL + "/" + MERCHANT_ID + "/initiatives/" + INITIATIVE_ID
                + "/point-of-sales/POS_ID")
        .header("x-point-of-sale-id", "DIFFERENT_POS_ID"))
        .andExpect(status().isForbidden());

    verifyNoInteractions(pointOfSaleFinderService, pointOfSaleInitiativeFinderService);
  }

  @Test
  void getPointOfSalesListMerchantMismatch_shouldReturnForbidden() throws Exception {
    String pathMerchantId = "MERCHANT_ID";
    String tokenMerchantId = "DIFFERENT_MERCHANT_ID";

    mockMvc.perform(
            MockMvcRequestBuilders.get(BASE_URL + "/" + pathMerchantId + "/point-of-sales")
                .header("x-merchant-id", tokenMerchantId)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andExpect(result -> Assertions.assertInstanceOf(
            MerchantNotAllowedException.class,
            result.getResolvedException()
        ))
        .andExpect(result -> Assertions.assertTrue(
            Objects.requireNonNull(result.getResolvedException()).getMessage()
                .contains("Merchant mismatch")
        ))
        .andReturn();
  }

  @Test
  void savePointOfSalesMerchantMismatch_shouldThrowException() throws Exception {
    String tokenMerchantId = "DIFFERENT_MERCHANT_ID";

    PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();

    mockMvc.perform(
              MockMvcRequestBuilders.post(BASE_URL + "/" + MERCHANT_ID + "/initiatives/" +  INITIATIVE_ID + "/point-of-sales")
                .header("x-merchant-id", tokenMerchantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(pointOfSaleDTO)))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(result -> Assertions.assertInstanceOf(
            MerchantNotAllowedException.class,
            result.getResolvedException()
        ))
        .andExpect(result -> Assertions.assertTrue(
            Objects.requireNonNull(result.getResolvedException()).getMessage()
                .contains("Merchant mismatch")
        ));
  }

  @Test
  void getPointOfSaleMerchantMismatch_shouldThrowException() throws Exception {
    String pathMerchantId = "MERCHANT_ID";
    String tokenMerchantId = "DIFFERENT_MERCHANT_ID";
    String pointOfSaleId = "POS_ID";

    mockMvc.perform(
            MockMvcRequestBuilders.get(BASE_URL + "/" + pathMerchantId + "/point-of-sales/" + pointOfSaleId)
                .header("x-merchant-id", tokenMerchantId)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(result -> Assertions.assertInstanceOf(
            MerchantNotAllowedException.class,
            result.getResolvedException()
        ))
        .andExpect(result -> Assertions.assertTrue(
            Objects.requireNonNull(result.getResolvedException()).getMessage()
                .contains("Merchant mismatch")
        ));
  }

  @Test
  void testConstructorWithMessage() {
    String message = "Test merchant mismatch";
    MerchantNotAllowedException ex = new MerchantNotAllowedException(message);

    assertNotNull(ex);
    assertEquals("MERCHANT_NOT_ALLOWED", ex.getCode());
    assertEquals(message, ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  void testConstructorWithPrintStackTraceAndCause() {
    String message = "Test message";
    Throwable cause = new RuntimeException("Cause");

    MerchantNotAllowedException ex = new MerchantNotAllowedException(message, true, cause);

    assertNotNull(ex);
    assertEquals("MERCHANT_NOT_ALLOWED", ex.getCode());
    assertEquals(message, ex.getMessage());
    assertEquals(cause, ex.getCause());
  }

  @Test
  void testConstructorWithCodeAndMessage() {
    String customCode = "CUSTOM_CODE";
    String message = "Custom message";

    MerchantNotAllowedException ex = new MerchantNotAllowedException(customCode, message);

    assertNotNull(ex);
    assertEquals(customCode, ex.getCode());
    assertEquals(message, ex.getMessage());
    assertNull(ex.getCause());
  }

  @Test
  void getPointOfSaleInitiativesDetail_OK() throws Exception {

    String pointOfSaleId = "POS_ID";
    String merchantId = MERCHANT_ID;

    PointOfSaleInitiativeDTO initiativeDTO = PointOfSaleInitiativeDTO.builder()
            .initiativeId("INITIATIVE_1")
            .initiativeName("Initiative Name")
            .organizationName("Organization")
            .status("ACTIVE")
            .build();

    PointOfSaleInitiativeListDTO responseDTO = PointOfSaleInitiativeListDTO.builder()
            .initiatives(List.of(initiativeDTO))
            .build();

    when(pointOfSaleInitiativeFinderService.getInitiativesByPointOfSaleId(pointOfSaleId, merchantId))
            .thenReturn(responseDTO);

    mockMvc.perform(
                    MockMvcRequestBuilders.get(BASE_URL + "/point-of-sale/initiatives")
                            .header("x-point-of-sale-id", pointOfSaleId)
                            .header("x-merchant-id", merchantId)
                            .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.initiatives").isArray())
            .andExpect(jsonPath("$.initiatives[0].initiativeId").value("INITIATIVE_1"))
            .andExpect(jsonPath("$.initiatives[0].initiativeName").value("Initiative Name"))
            .andExpect(jsonPath("$.initiatives[0].organizationName").value("Organization"))
            .andExpect(jsonPath("$.initiatives[0].status").value("ACTIVE"))
            .andDo(print())
            .andReturn();
  }

  @Test
  void onboardingPointOfSales_OK() throws Exception {

    String merchantId = MERCHANT_ID;
    String initiativeId = "INITIATIVE_1";

    List<String> posIds = List.of("POS1", "POS2");

    AssociatedPointOfSaleDTO associated = AssociatedPointOfSaleDTO.builder()
            .pointOfSaleId("POS1")
            .franchiseName("Shop 1")
            .build();

    NotAssociatedPointOfSaleDTO notAssociated = NotAssociatedPointOfSaleDTO.builder()
            .pointOfSaleId("POS2")
            .franchiseName("Shop 2")
            .reason(PosOnbordingRejectionReason.ALREADY_ASSOCIATED)
            .address("ADDR")
            .city("CITY")
            .streetNumber("1")
            .build();

    PointOfSaleOnboardingResultDTO responseDTO = new PointOfSaleOnboardingResultDTO();
    responseDTO.setAssociated(List.of(associated));
    responseDTO.setNotAssociated(List.of(notAssociated));

    when(pointOfSaleWriterMock.onboardingPointOfSales(
            merchantId,
            initiativeId,
            posIds
    )).thenReturn(responseDTO);

    mockMvc.perform(
                    MockMvcRequestBuilders.post(
                                    BASE_URL + "/{merchantId}/initiatives/{initiativeId}/point-of-sales/onboarding",
                                    merchantId,
                                    initiativeId
                            )
                            .header("x-merchant-id", merchantId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(posIds))
                            .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))

            .andExpect(jsonPath("$.associated").isArray())
            .andExpect(jsonPath("$.associated[0].pointOfSaleId").value("POS1"))
            .andExpect(jsonPath("$.associated[0].franchiseName").value("Shop 1"))

            .andExpect(jsonPath("$.notAssociated").isArray())
            .andExpect(jsonPath("$.notAssociated[0].pointOfSaleId").value("POS2"))
            .andExpect(jsonPath("$.notAssociated[0].reason").value("ALREADY_ASSOCIATED"))

            .andDo(print())
            .andReturn();
  }

  @Test
  void onboardingPointOfSalesMerchantMismatch_shouldReturnForbidden() throws Exception {
    mockMvc.perform(
            MockMvcRequestBuilders.post(
                    BASE_URL + "/{merchantId}/initiatives/{initiativeId}/point-of-sales/onboarding",
                    MERCHANT_ID,
                    "INITIATIVE_1"
                )
                .header("x-merchant-id", "DIFFERENT_MERCHANT_ID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of("POS1")))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden());

    verify(pointOfSaleWriterMock, never()).onboardingPointOfSales(any(), any(), any());
  }

  @Test
  void excludePointsOfSales_OK() throws Exception {
    List<String> posIds = List.of("POS1", "POS2");

    ExcludedPointOfSaleDetailDTO excludedPos = ExcludedPointOfSaleDetailDTO.builder()
            .pointOfSaleId("POS1")
            .franchiseName("Negozio Escluso 1")
            .build();

    NotExcludedPointOfSaleDTO notExcludedPos = NotExcludedPointOfSaleDTO.builder()
            .pointOfSaleId("POS2")
            .reason(it.gov.pagopa.merchant.dto.enums.PosOnbordingExclusionRejectionReason.HAS_TRANSACTIONS)
            .build();

    PointOfSaleExclusionResultDTO responseDTO = PointOfSaleExclusionResultDTO.builder()
            .excludedPointOfSales(List.of(excludedPos))
            .notExcludedPointOfSales(List.of(notExcludedPos))
            .build();

    when(pointOfSaleWriterMock.excludePointsOfSales(
            MERCHANT_ID,
            INITIATIVE_ID,
            posIds
    )).thenReturn(responseDTO);

    mockMvc.perform(
                    MockMvcRequestBuilders.post(
                                    BASE_URL + "/{merchantId}/initiatives/{initiativeId}/point-of-sales/exclusion",
                                    MERCHANT_ID,
                                    INITIATIVE_ID
                            )
                            .header("x-merchant-id", MERCHANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(posIds))
                            .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.excludedPointOfSales").isArray())
            .andExpect(jsonPath("$.excludedPointOfSales[0].pointOfSaleId").value("POS1"))
            .andExpect(jsonPath("$.excludedPointOfSales[0].franchiseName").value("Negozio Escluso 1"))
            .andExpect(jsonPath("$.notExcludedPointOfSales").isArray())
            .andExpect(jsonPath("$.notExcludedPointOfSales[0].pointOfSaleId").value("POS2"))
            .andExpect(jsonPath("$.notExcludedPointOfSales[0].reason").value("HAS_TRANSACTIONS"))
            .andDo(print())
            .andReturn();

    verify(pointOfSaleWriterMock).excludePointsOfSales(MERCHANT_ID, INITIATIVE_ID, posIds);
  }

  @Test
  void excludePointsOfSalesMerchantMismatch_shouldReturnForbidden() throws Exception {
    List<String> posIds = List.of("POS1");

    mockMvc.perform(
                    MockMvcRequestBuilders.post(
                                    BASE_URL + "/{merchantId}/initiatives/{initiativeId}/point-of-sales/exclusion",
                                    MERCHANT_ID,
                                    INITIATIVE_ID
                            )
                            .header("x-merchant-id", "DIFFERENT_MERCHANT_ID")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(posIds))
                            .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());

    verify(pointOfSaleWriterMock, never()).excludePointsOfSales(any(), any(), any());
  }
}

