package it.gov.pagopa.merchant.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.merchant.configuration.JsonConfig;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.test.fakers.InitiativeDTOFaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MerchantControllerImpl.class)
@Import(JsonConfig.class)
class MerchantcontrollerImplTest {

    public static final String MERCHANT_ID = "MERCHANT_ID";

    @MockBean
    private MerchantService merchantService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getMerchantInitiativeList_ok() throws Exception {
        List<InitiativeDTO> expectedResult = List.of(
                InitiativeDTOFaker.mockInstance(1),
                InitiativeDTOFaker.mockInstance(2));

        when(merchantService.getMerchantInitiativeList(anyString())).thenReturn(expectedResult);

        MvcResult result = mockMvc.perform(
                get("/idpay/merchant/initiatives")
                        .header("x-merchant-id", MERCHANT_ID))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        List<InitiativeDTO> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        assertEquals(expectedResult, response);

    }

    @Test
    void getMerchantInitiativeList_ko() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/idpay/merchant/initiatives"))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertEquals("Required header 'x-merchant-id' is not present.",
                result.getResponse().getErrorMessage());
    }
}
