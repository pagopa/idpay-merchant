package it.gov.pagopa.merchant.controller.merchant_portal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.common.config.JsonConfig;
import it.gov.pagopa.common.web.dto.ErrorDTO;
import it.gov.pagopa.merchant.configuration.MerchantErrorManagerConfig;
import it.gov.pagopa.merchant.configuration.ServiceExceptionConfig;
import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode;
import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionMessage;
import it.gov.pagopa.merchant.dto.InitiativeDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.service.ReportedUserService;
import it.gov.pagopa.merchant.test.fakers.InitiativeDTOFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MerchantPortalMerchantControllerImpl.class)
@Import({JsonConfig.class, ServiceExceptionConfig.class, MerchantErrorManagerConfig.class})
class MerchantPortalMerchantControllerImplTest {
    @MockitoBean private MerchantService merchantServiceMock;
    @MockitoBean private ReportedUserService reportedUserService;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String MERCHANT_ID = "MERCHANT_ID";
    private static final String INITIATIVE_ID = "INITIATIVE_ID";


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
    void getMerchantInitiativeList_noHeader() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/idpay/merchant/portal/initiatives"))
                .andExpect(status().isBadRequest())
                .andReturn();

        Assertions.assertEquals("Required request header 'x-merchant-id' for method parameter type String is not present",
                Objects.requireNonNull(result.getResolvedException()).getMessage());
    }

    @Test
    void getMerchantDetailByMerchantIdAndInitiativeId() throws Exception {
        MerchantDetailDTO merchantDetailDTO = MerchantDetailDTOFaker.mockInstanceBuilder(1)
                .initiativeId(INITIATIVE_ID)
                .build();
        Mockito.when(merchantServiceMock.getMerchantDetail(MERCHANT_ID, INITIATIVE_ID)).thenReturn(merchantDetailDTO);

        MvcResult result = mockMvc.perform(
                get("/idpay/merchant/portal/initiatives/{initiativeId}", INITIATIVE_ID)
                        .header("x-merchant-id", MERCHANT_ID)
        ).andExpect(status().is2xxSuccessful()).andReturn();

        MerchantDetailDTO resultResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                MerchantDetailDTO.class);

        Assertions.assertNotNull(resultResponse);
        Assertions.assertEquals(merchantDetailDTO,resultResponse);
        Mockito.verify(merchantServiceMock).getMerchantDetail(anyString(), anyString());
    }

    @Test
    void getMerchantDetailByMerchantIdAndInitiativeId_notFound() throws Exception {
        Mockito.when(merchantServiceMock.getMerchantDetail(MERCHANT_ID, INITIATIVE_ID))
                .thenReturn(null);

        MvcResult result = mockMvc.perform(
                        get("/idpay/merchant/portal/initiatives/{initiativeId}", INITIATIVE_ID)
                                .header("x-merchant-id", MERCHANT_ID))
                .andExpect(status().isNotFound())
                .andExpect(res -> Assertions.assertTrue(res.getResolvedException() instanceof MerchantNotFoundException))
                .andReturn();

        ErrorDTO errorDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ErrorDTO.class
        );

        Assertions.assertEquals(ExceptionCode.MERCHANT_NOT_ONBOARDED, errorDTO.getCode());
        Assertions.assertEquals(String.format(ExceptionMessage.INITIATIVE_AND_MERCHANT_NOT_FOUND, INITIATIVE_ID),errorDTO.getMessage());
        Mockito.verify(merchantServiceMock).getMerchantDetail(anyString(), anyString());
    }
}
