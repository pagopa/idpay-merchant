package it.gov.pagopa.merchant.controller;


import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.test.faker.MerchantFaker;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.merchant.configuration.JsonConfig;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MerchantControllerImpl.class)
@Import(JsonConfig.class)
class MerchantControllerImplTest {
    @MockBean private MerchantService merchantServiceMock;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

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
    void getMerchantDetail_notFound() throws Exception {
        Mockito.when(merchantService.getMerchantDetail(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new ClientExceptionWithBody(HttpStatus.NOT_FOUND,
                        MerchantConstants.NOT_FOUND,
                        String.format(MerchantConstants.INITIATIVE_AND_MERCHANT_NOT_FOUND, INITIATIVE_ID, MERCHANT_ID)));

        mockMvc.perform(
                        get("/idpay/merchant/{initiativeId}/{merchantId}/detail", INITIATIVE_ID, MERCHANT_ID)
                ).andExpect(status().isNotFound())
                .andExpect(res -> Assertions.assertTrue(res.getResolvedException() instanceof ClientExceptionWithBody))
                .andReturn();

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

    @Test
    void retrieveMerchantIdOK() throws Exception {
        Merchant merchant = MerchantFaker.mockInstance(1);

        Mockito.when(merchantServiceMock.retrieveMerchantId(merchant.getFiscalCode(), merchant.getAcquirerId())).thenReturn(merchant.getMerchantId());

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/idpay/merchant/merchantId/{fiscalCode}/{acquirerId}",merchant.getFiscalCode(), merchant.getAcquirerId()))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        Assertions.assertEquals(merchant.getMerchantId(), result.getResponse().getContentAsString());
    }

    /* TODO errorHandler
    @Test
    void retrieveMerchantIdNotFoundException() throws Exception {
        Mockito.when(merchantServiceMock.retrieveMerchantId("FISCALCODE", "ACQUIRERID"))
                .thenThrow(new MerchantException("CODE", "MESSAGE", HttpStatus.NOT_FOUND));

        mockMvc.perform(
                MockMvcRequestBuilders.get("/idpay/merchant/merchantId/{fiscalCode}/{acquirerId}", "FISCALCODE", "ACQUIRERID"))
                .andExpect(status().isNotFound());
    }
    */
}