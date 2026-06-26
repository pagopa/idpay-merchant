package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.common.config.JsonConfig;
import it.gov.pagopa.merchant.configuration.ServiceExceptionConfig;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotAllowedException;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleNotAllowedException;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleNotFoundException;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.service.merchant.MerchantDetailService;
import it.gov.pagopa.merchant.service.pointofsales.GetPointOfSaleService;
import it.gov.pagopa.merchant.service.pointofsales.GetPointOfSaleWithInitiativeService;
import it.gov.pagopa.merchant.service.pointofsales.SavePointOfSaleService;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleDTOFaker;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleFaker;
import it.gov.pagopa.merchant.utils.validator.PointOfSaleValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

import java.util.List;
import java.util.Objects;

import static it.gov.pagopa.merchant.constants.PointOfSaleConstants.MSG_NOT_FOUND;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value={PointOfSaleControllerImpl.class}, excludeAutoConfiguration = { UserDetailsServiceAutoConfiguration.class , SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({JsonConfig.class, ServiceExceptionConfig.class})
class PointOfSaleControllerImplTest {

    @MockitoBean
    private GetPointOfSaleService getPointOfSaleService;
    @MockitoBean
    private GetPointOfSaleWithInitiativeService getPointOfSaleWithInitiativeService;
    @MockitoBean
    private MerchantDetailService merchantDetailService;
    @MockitoBean
    private PointOfSaleValidator validator;
    @MockitoBean
    private PointOfSaleDTOMapper mapper;
    @MockitoBean
    private MerchantService merchantService;
    @MockitoBean
    private SavePointOfSaleService savePointOfSaleServiceMock;

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

    doNothing().when(savePointOfSaleServiceMock).savePointOfSales(MERCHANT_ID, INITIATIVE_ID, List.of(pointOfSaleDTO));

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

    verify(savePointOfSaleServiceMock).savePointOfSales(anyString(), anyString(), anyList());
  }

  @Test
  void getPointOfSalesListOK() throws Exception {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    PageRequest pageRequest = PageRequest.of(0, 10);
    Page<PointOfSale> expectedPage = new PageImpl<>(List.of(pointOfSale), pageRequest, 1);

    when(getPointOfSaleService.getPointOfSalesList(any(), any(), any(), any(), any(),
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
    when(getPointOfSaleService.getPointOfSalesList(any(), any(), any(), any(), any(), any()))
        .thenReturn(Page.empty());

    mockMvc.perform(
            MockMvcRequestBuilders.get(BASE_URL + String.format(GET_POINT_OF_SALES, MERCHANT_ID))
                .header("x-merchant-id", MERCHANT_ID))
        .andExpect(status().isOk());

    verify(getPointOfSaleService).getPointOfSalesList(eq(MERCHANT_ID), any(), any(), any(), any(),
        any());
  }

  @Test
  void getPointOfSalesListWithInitiativeQueryParam_shouldUseInitiativeFilter() throws Exception {
    when(getPointOfSaleWithInitiativeService.getPointOfSalesListByInitiative(
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
    verify(getPointOfSaleWithInitiativeService).getPointOfSalesListByInitiative(
        eq(INITIATIVE_ID), eq(MERCHANT_ID), eq("Fisico"), eq("Rieti"), eq("Via Nome"),
        eq("Mario Rossi"), pageableCaptor.capture());
    verifyNoInteractions(getPointOfSaleService);
    assertEquals(1, pageableCaptor.getValue().getPageNumber());
    assertEquals(10, pageableCaptor.getValue().getPageSize());
    Assertions.assertTrue(pageableCaptor.getValue().getSort().isSorted());
  }

  @Test
  void getPointOfSalesListByInitiativeOK() throws Exception {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    PageRequest pageRequest = PageRequest.of(0, 10);
    Page<PointOfSale> expectedPage = new PageImpl<>(List.of(pointOfSale), pageRequest, 1);

    when(getPointOfSaleWithInitiativeService.getPointOfSalesListByInitiative(eq(INITIATIVE_ID), eq(MERCHANT_ID),
        any(), any(), any(), any(), any())).thenReturn(expectedPage);

    mockMvc.perform(MockMvcRequestBuilders.get(
            BASE_URL + "/" + MERCHANT_ID + "/initiatives/" + INITIATIVE_ID + "/point-of-sales"))
        .andExpect(status().isOk());

    verify(getPointOfSaleWithInitiativeService).getPointOfSalesListByInitiative(eq(INITIATIVE_ID), eq(MERCHANT_ID),
        any(), any(), any(), any(), any());
  }

  @Test
  void getPointOfSalesListByInitiativeMerchantMismatch_shouldReturnForbidden() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get(
            BASE_URL + "/" + MERCHANT_ID + "/initiatives/" + INITIATIVE_ID + "/point-of-sales")
        .header("x-merchant-id", "DIFFERENT_MERCHANT_ID"))
        .andExpect(status().isForbidden());

    verifyNoInteractions(getPointOfSaleService, getPointOfSaleWithInitiativeService);
  }

    @Test
    void getPointOfSaleTestOK() throws Exception {
        PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
        Merchant merchant = Mockito.mock(Merchant.class);
        PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();

        when(getPointOfSaleService.getPointOfSaleByIdAndMerchantId(anyString(), anyString()))
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

        Mockito.verify(getPointOfSaleService).getPointOfSaleByIdAndMerchantId(anyString(), anyString());
        verify(merchantService).getMerchantByMerchantId(MERCHANT_ID);
        Mockito.verify(mapper).entityToDto(pointOfSale, merchant);
    }

  @Test
  void getPointOfSaleTestKO() throws Exception {
    String invalidPosId = "INVALID_POS_ID";

    when(getPointOfSaleService.getPointOfSaleByIdAndMerchantId(anyString(), anyString()))
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

    verify(getPointOfSaleService).getPointOfSaleByIdAndMerchantId(anyString(), anyString());
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

    when(getPointOfSaleService.getPointOfSaleByIdAndMerchantId(anyString(), anyString()))
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

    verify(getPointOfSaleService).getPointOfSaleByIdAndMerchantId("POS_ID", "MERCHANT_ID");
    verify(merchantService).getMerchantByMerchantId("MERCHANT_ID");
    verify(mapper).entityToDto(pointOfSale, merchant);
  }

  @Test
  void getPointOfSaleByInitiativeOK() throws Exception {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    Merchant merchant = new Merchant();
    PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();

    when(getPointOfSaleWithInitiativeService.getPointOfSaleByIdAndMerchantIdAndInitiativeId(
        INITIATIVE_ID, "POS_ID", MERCHANT_ID)).thenReturn(pointOfSale);
    when(merchantService.getMerchantByMerchantId(MERCHANT_ID)).thenReturn(merchant);
    when(mapper.entityToDto(pointOfSale, merchant)).thenReturn(pointOfSaleDTO);

    mockMvc.perform(MockMvcRequestBuilders.get(
            BASE_URL + "/" + MERCHANT_ID + "/initiatives/" + INITIATIVE_ID
                + "/point-of-sales/POS_ID"))
        .andExpect(status().isOk());

    verify(getPointOfSaleWithInitiativeService).getPointOfSaleByIdAndMerchantIdAndInitiativeId(
        INITIATIVE_ID, "POS_ID", MERCHANT_ID);
  }

  @Test
  void getPointOfSaleByInitiativeNotFound() throws Exception {
    when(getPointOfSaleWithInitiativeService.getPointOfSaleByIdAndMerchantIdAndInitiativeId(
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

    verifyNoInteractions(getPointOfSaleService, getPointOfSaleWithInitiativeService);
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
}

