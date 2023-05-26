package it.gov.pagopa.merchant.controller.merchant_portal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.merchant.configuration.JsonConfig;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.test.fakers.InitiativeDTOFaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MerchantPortalMerchantControllerImpl.class)
@Import(JsonConfig.class)
class MerchantPortalMerchantControllerImplTest {
    @MockBean private MerchantService merchantServiceMock;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String MERCHANT_ID = "MERCHANT_ID";

    @Test
    void getMerchantInitiativeList() throws Exception {
        List<InitiativeDTO> expectedResult = List.of(
                InitiativeDTOFaker.mockInstance(1),
                InitiativeDTOFaker.mockInstance(2));

        Mockito.when(merchantServiceMock.getMerchantInitiativeList(anyString())).thenReturn(expectedResult);

        MvcResult result = mockMvc.perform(
                        get("/idpay/merchant/portal/initiatives")
                                .header("x-merchant-id", MERCHANT_ID))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        List<InitiativeDTO> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        Assertions.assertEquals(expectedResult, response);
    }

    @Test
    void getMerchantInitiativeList_notFound() throws Exception {
        Mockito.when(merchantServiceMock.getMerchantInitiativeList(anyString())).thenReturn(null);

        mockMvc.perform(
                        get("/idpay/merchant/portal/initiatives")
                                .header("x-merchant-id", MERCHANT_ID))
                .andExpect(status().isNotFound())
                .andExpect(res -> Assertions.assertTrue(res.getResolvedException() instanceof ClientExceptionWithBody))
                .andExpect(res -> Assertions.assertEquals(String.format(MerchantConstants.MERCHANT_BY_MERCHANT_ID_MESSAGE, MERCHANT_ID),
                        Objects.requireNonNull(res.getResolvedException()).getMessage()))
                .andReturn();

        Mockito.verify(merchantServiceMock).getMerchantInitiativeList(anyString());
    }

    @Test
    void getMerchantInitiativeList_noHeader() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/idpay/merchant/portal/initiatives"))
                .andExpect(status().isBadRequest())
                .andReturn();

        Assertions.assertEquals("Required request header 'x-merchant-id' for method parameter type String is not present",
                Objects.requireNonNull(result.getResolvedException()).getMessage());
    }


}
