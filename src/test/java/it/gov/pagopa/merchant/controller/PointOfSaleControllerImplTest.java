package it.gov.pagopa.merchant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.common.config.JsonConfig;
import it.gov.pagopa.merchant.configuration.ServiceExceptionConfig;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleListDTO;
import it.gov.pagopa.merchant.service.pointofsales.PointOfSaleService;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleDTOFaker;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleListDTOFaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(PointOfSaleControllerImpl.class)
@Import({JsonConfig.class, ServiceExceptionConfig.class})
class PointOfSaleControllerImplTest {

    @MockBean
    private PointOfSaleService pointOfSaleService;

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
        PointOfSaleDTO pointOfSaleDTOFaker = PointOfSaleDTOFaker.mockInstance();

        doNothing().when(pointOfSaleService).savePointOfSales(MERCHANT_ID, List.of(pointOfSaleDTOFaker));

        mockMvc.perform(
                        MockMvcRequestBuilders.put(BASE_URL + String.format(SAVE_POINT_OF_SALES, MERCHANT_ID))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(List.of(pointOfSaleDTOFaker)))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andDo(print())
                .andReturn();
    }

    @Test
    void savePointOfSalesKO() throws Exception {

        mockMvc.perform(
                        MockMvcRequestBuilders.put(BASE_URL + String.format(SAVE_POINT_OF_SALES, MERCHANT_ID))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(List.of()))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andDo(print())
                .andReturn();
    }

    @Test
    void getPointOfSalesListOK() throws Exception {

        PointOfSaleListDTO pointOfSaleListDTO = PointOfSaleListDTOFaker.mockInstance();

        Mockito.when(pointOfSaleService.getPointOfSalesList(any(),any(),any(),any(),any(),any())).thenReturn(pointOfSaleListDTO);

        MvcResult result =
                mockMvc.perform(
                                MockMvcRequestBuilders.get(BASE_URL + String.format(GET_POINT_OF_SALES, MERCHANT_ID)))
                        .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                        .andDo(print())
                        .andReturn();
        Assertions.assertNotNull(result);
    }


}