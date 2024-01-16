package it.gov.pagopa.merchant.controller.acquirer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.common.config.JsonConfig;
import it.gov.pagopa.merchant.configuration.MerchantErrorManagerConfig;
import it.gov.pagopa.merchant.configuration.ServiceExceptionConfig;
import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionMessage;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.test.fakers.InitiativeDTOFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantUpdateDTOFaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AcquirerControllerImpl.class)
@Import({JsonConfig.class, ServiceExceptionConfig.class, MerchantErrorManagerConfig.class})
class AcquirerControllerImplTest {
    @MockBean private MerchantService merchantServiceMock;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String MERCHANT_ID = "MERCHANT_ID";
    private static final String INITIATIVE_ID = "INITIATIVE_ID";
    private static final String ACQUIRER_ID = "ACQUIRER_ID";


    @Test
    void getMerchantInitiativeList() throws Exception {
        List<InitiativeDTO> expectedResult = List.of(
                InitiativeDTOFaker.mockInstance(1),
                InitiativeDTOFaker.mockInstance(2));

        Mockito.when(merchantServiceMock.getMerchantInitiativeList(anyString())).thenReturn(expectedResult);

        MvcResult result = mockMvc.perform(
                        get("/idpay/merchant/acquirer/initiatives")
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
                        get("/idpay/merchant/acquirer/initiatives")
                                .header("x-merchant-id", MERCHANT_ID))
                .andExpect(status().isNotFound())
                .andExpect(res -> Assertions.assertTrue(res.getResolvedException() instanceof MerchantNotFoundException))
                .andExpect(res -> Assertions.assertEquals(ExceptionMessage.MERCHANT_NOT_FOUND_MESSAGE,
                        Objects.requireNonNull(res.getResolvedException()).getMessage()))
                .andReturn();

        Mockito.verify(merchantServiceMock).getMerchantInitiativeList(anyString());
    }

    @Test
    void getMerchantInitiativeList_noHeader() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/idpay/merchant/acquirer/initiatives"))
                .andExpect(status().isBadRequest())
                .andReturn();

        Assertions.assertEquals("Required request header 'x-merchant-id' for method parameter type String is not present",
                Objects.requireNonNull(result.getResolvedException()).getMessage());
    }

    @Test
    void uploadMerchantFile() throws  Exception {
        MerchantUpdateDTO merchantUpdateDTO = MerchantUpdateDTOFaker.mockInstance(1);
        merchantUpdateDTO.setStatus("VALIDATED");
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "content".getBytes());
        Mockito.when(merchantServiceMock.uploadMerchantFile(file, ACQUIRER_ID, INITIATIVE_ID, null, ACQUIRER_ID)).thenReturn(merchantUpdateDTO);

        MockMultipartHttpServletRequestBuilder builder = multipart("/idpay/merchant/acquirer/{acquirerId}/initiative/{initiativeId}/upload",
                ACQUIRER_ID, INITIATIVE_ID);
        builder.with(
                request -> {
                    request.setMethod("PUT");
                    return request;
                }
        );

        MvcResult result = mockMvc.perform(builder.file(file))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andReturn();

        //MerchantUpdateDTO resultDTO = objectMapper.readValue(result.getResponse().getContentAsString(), MerchantUpdateDTO.class);

        //Assertions.assertEquals(merchantUpdateDTO, resultDTO);
        Mockito.verify(merchantServiceMock).uploadMerchantFile(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.isNull(), Mockito.anyString());
    }


}
