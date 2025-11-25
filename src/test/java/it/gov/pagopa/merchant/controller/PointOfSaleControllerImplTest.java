package it.gov.pagopa.merchant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.common.config.JsonConfig;
import it.gov.pagopa.merchant.configuration.ServiceExceptionConfig;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleNotAllowedException;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleNotFoundException;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.service.merchant.MerchantDetailService;
import it.gov.pagopa.merchant.service.pointofsales.PointOfSaleService;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleDTOFaker;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleFaker;
import it.gov.pagopa.merchant.utils.validator.PointOfSaleValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Objects;

import static it.gov.pagopa.merchant.constants.PointOfSaleConstants.MSG_NOT_FOUND;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointOfSaleControllerImpl.class)
@Import({JsonConfig.class, ServiceExceptionConfig.class})
class PointOfSaleControllerImplTest {

  @MockitoBean
  private PointOfSaleService pointOfSaleService;
  @MockitoBean
  private MerchantDetailService merchantDetailService;
  @MockitoBean
  private PointOfSaleValidator validator;
  @MockitoBean
  private PointOfSaleDTOMapper mapper;

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  private static final String BASE_URL = "/idpay/merchant/portal";
  private static final String SAVE_POINT_OF_SALES = "/%s/point-of-sales";
  private static final String GET_POINT_OF_SALES = "/%s/point-of-sales";

  private static final String MERCHANT_ID = "MERCHANT_ID";

  @Test
  void savePointOfSalesOK() throws Exception {
    PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();
    PointOfSale pointOfSaleFaker = PointOfSaleFaker.mockInstance();

    doNothing().when(validator).validatePointOfSales(any());

    doNothing().when(pointOfSaleService).savePointOfSales(MERCHANT_ID, List.of(pointOfSaleFaker));

    mockMvc.perform(
            MockMvcRequestBuilders.put(BASE_URL + String.format(SAVE_POINT_OF_SALES, MERCHANT_ID))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(List.of(pointOfSaleDTO)))
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful())
        .andDo(print())
        .andReturn();
  }

  @Test
  void getPointOfSalesListOK() throws Exception {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    PageRequest pageRequest = PageRequest.of(0, 10);
    Page<PointOfSale> expectedPage = new PageImpl<>(List.of(pointOfSale), pageRequest, 1);

    when(pointOfSaleService.getPointOfSalesList(any(), any(), any(), any(), any(),
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
  void getPointOfSaleTestOK() throws Exception {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();

    when(pointOfSaleService.getPointOfSaleByIdAndMerchantId(anyString(), anyString()))
        .thenReturn(pointOfSale);
    when(mapper.entityToDto(pointOfSale)).thenReturn(pointOfSaleDTO);

    MvcResult result = mockMvc.perform(
            MockMvcRequestBuilders.get(BASE_URL + "/MERCHANT_ID/point-of-sales/POS_ID")
                .header("x-point-of-sale-id", "POS_ID")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(print())
        .andReturn();

    Assertions.assertNotNull(result);

    verify(pointOfSaleService).getPointOfSaleByIdAndMerchantId(anyString(), anyString());
    verify(mapper).entityToDto(pointOfSale);
  }

  @Test
  void getPointOfSaleTestKO() throws Exception {
    String invalidPosId = "INVALID_POS_ID";

    when(pointOfSaleService.getPointOfSaleByIdAndMerchantId(anyString(), anyString()))
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

    verify(pointOfSaleService).getPointOfSaleByIdAndMerchantId(anyString(), anyString());
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
                .contains("not authorized for the current token")
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
  void getPointOfSaleTestWithNullHeaderPosId() throws Exception {
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();

    when(pointOfSaleService.getPointOfSaleByIdAndMerchantId(anyString(), anyString()))
        .thenReturn(pointOfSale);
    when(mapper.entityToDto(pointOfSale)).thenReturn(pointOfSaleDTO);

    MvcResult result = mockMvc.perform(
            MockMvcRequestBuilders.get(BASE_URL + "/MERCHANT_ID/point-of-sales/POS_ID")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    Assertions.assertNotNull(result);

    verify(pointOfSaleService).getPointOfSaleByIdAndMerchantId(anyString(), anyString());
    verify(mapper).entityToDto(pointOfSale);
  }
}

