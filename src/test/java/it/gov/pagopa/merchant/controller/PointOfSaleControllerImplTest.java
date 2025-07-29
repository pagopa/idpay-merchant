package it.gov.pagopa.merchant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.common.config.JsonConfig;
import it.gov.pagopa.merchant.configuration.ServiceExceptionConfig;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDetailDTO;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

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
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andDo(print())
                .andReturn();
    }


    @Test
    void getPointOfSalesListOK() throws Exception {
        PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<PointOfSale> expectedPage = new PageImpl<>(List.of(pointOfSale), pageRequest, 1);

        when(pointOfSaleService.getPointOfSalesList(any(), any(), any(), any(), any(), any())).thenReturn(expectedPage);

        MvcResult result =
                mockMvc.perform(
                                MockMvcRequestBuilders.get(BASE_URL + String.format(GET_POINT_OF_SALES, MERCHANT_ID)))
                        .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                        .andDo(print())
                        .andReturn();
        Assertions.assertNotNull(result);
    }

    @Test
    void getPointOfSaleDetailByIdOK() throws Exception {

        PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
        PointOfSaleDTO pointOfSaleDTO = PointOfSaleDTOFaker.mockInstance();

        MerchantDetailDTO merchantDetailDTO = MerchantDetailDTO.builder()
                .vatNumber("12345678901")
                .iban("IT60X0542811101000000123456")
                .build();

        PointOfSaleDetailDTO expectedDTO = PointOfSaleDetailDTO.builder()
                .pointOfSale(pointOfSaleDTO)
                .merchantDetail(merchantDetailDTO)
                .build();

        when(pointOfSaleService.getPointOfSaleById("POS_ID")).thenReturn(pointOfSale);
        when(merchantDetailService.getMerchantDetail(MERCHANT_ID)).thenReturn(merchantDetailDTO);
        when(mapper.pointOfSaleEntityToPointOfSaleDTO(pointOfSale)).thenReturn(pointOfSaleDTO);

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL + "/" + MERCHANT_ID + "/point-of-sales/POS_ID")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(print())
                .andReturn();
        Assertions.assertNotNull(result);
    }
}