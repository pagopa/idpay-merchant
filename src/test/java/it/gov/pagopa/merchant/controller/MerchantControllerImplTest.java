package it.gov.pagopa.merchant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.merchant.configuration.JsonConfig;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MerchantControllerImpl.class)
@Import(JsonConfig.class)
class MerchantControllerImplTest {
    @MockBean
    private MerchantService merchantService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String INITIATIVE_ID = "INITIATIVE_ID";
    private static final String MERCHANT_ID = "MERCHANT_ID";
    private static final String FISCAL_CODE = "FISCAL_CODE";


    @Test
    void getMerchantDetail() throws Exception {
        MerchantDetailDTO dto = MerchantDetailDTOFaker.mockInstance(1);
        Mockito.when(merchantService.getMerchantDetail(Mockito.anyString(), Mockito.anyString())).thenReturn(dto);

        MvcResult result = mockMvc.perform(
                get("/idpay/merchant/{initiativeId}/{merchantId}/detail", INITIATIVE_ID, MERCHANT_ID)
        ).andExpect(status().is2xxSuccessful()).andReturn();

        MerchantDetailDTO resultResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                MerchantDetailDTO.class);

        Assertions.assertNotNull(resultResponse);
        Assertions.assertEquals(dto,resultResponse);
        Mockito.verify(merchantService).getMerchantDetail(anyString(),anyString());
    }

    @Test
    void getMerchantList() throws Exception {
        MerchantListDTO dto = MerchantListDTO.builder().content(Collections.emptyList())
                .pageNo(1).pageSize(1).totalElements(1).totalPages(1).build();
        Mockito.when(merchantService.getMerchantList(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn(dto);

        MvcResult result = mockMvc.perform(
                get("/idpay/merchant/{initiativeId}", INITIATIVE_ID)
                        .param("fiscalCode", FISCAL_CODE)
                        .param("page", String.valueOf(1))
                        .param("size", String.valueOf(10))
        ).andExpect(status().is2xxSuccessful()).andReturn();

        MerchantListDTO resultResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                MerchantListDTO.class);

        Assertions.assertNotNull(resultResponse);
        Assertions.assertEquals(dto,resultResponse);
        Mockito.verify(merchantService).getMerchantList(anyString(),anyString(), any());
    }
}