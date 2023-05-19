package it.gov.pagopa.merchant.controller;

import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.test.faker.MerchantFaker;
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
class MerchantControllerImplTest {
    @MockBean private MerchantService merchantServiceMock;

    @Autowired private MockMvc mockMvc;

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