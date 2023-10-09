package it.gov.pagopa.merchant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.common.config.JsonConfig;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.common.web.dto.ErrorDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantUpdateDTOFaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@Disabled
@WebMvcTest(MerchantControllerImpl.class)
@Import(JsonConfig.class)
class MerchantControllerImplTest {
    @MockBean private MerchantService merchantServiceMock;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String INITIATIVE_ID = "INITIATIVE_ID";
    private static final String ORGANIZATION_ID = "ORGANIZATION_ID";
    private static final String ACQUIRER_ID = "PAGOPA";
    private static final String MERCHANT_ID = "MERCHANT_ID";
    private static final String FISCAL_CODE = "FISCAL_CODE";

    @Test
    void uploadMerchantFile() throws  Exception {
        MerchantUpdateDTO merchantUpdateDTO = MerchantUpdateDTOFaker.mockInstance(1);
        merchantUpdateDTO.setStatus("VALIDATED");
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "content".getBytes());
        Mockito.when(merchantServiceMock.uploadMerchantFile(file, ORGANIZATION_ID, INITIATIVE_ID, "ORGANIZATION_USER_ID", ACQUIRER_ID)).thenReturn(merchantUpdateDTO);

        MockMultipartHttpServletRequestBuilder builder = multipart("/idpay/merchant/organization/{organizationId}/initiative/{initiativeId}/upload",
                ORGANIZATION_ID, INITIATIVE_ID);
        builder.header("organization-user-id", "organizationUserId");
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
        Mockito.verify(merchantServiceMock).uploadMerchantFile(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }
    @Test
    void getMerchantDetail() throws Exception {
        MerchantDetailDTO dto = MerchantDetailDTOFaker.mockInstance(1);
        Mockito.when(merchantServiceMock.getMerchantDetail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(dto);

        MvcResult result = mockMvc.perform(
                get("/idpay/merchant/{merchantId}/organization/{organizationId}/initiative/{initiativeId}",
                        MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID)
        ).andExpect(status().is2xxSuccessful()).andReturn();

        MerchantDetailDTO resultResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                MerchantDetailDTO.class);

        Assertions.assertNotNull(resultResponse);
        Assertions.assertEquals(dto,resultResponse);
        Mockito.verify(merchantServiceMock).getMerchantDetail(anyString(), anyString(), anyString());
    }
    @Test
    void getMerchantDetail_notFound() throws Exception {
        Mockito.when(merchantServiceMock.getMerchantDetail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new ClientExceptionWithBody(HttpStatus.NOT_FOUND,
                        MerchantConstants.NOT_FOUND,
                        String.format(MerchantConstants.INITIATIVE_AND_MERCHANT_NOT_FOUND, INITIATIVE_ID, MERCHANT_ID)));

        mockMvc.perform(
                get("/idpay/merchant/{merchantId}/organization/{organizationId}/initiative/{initiativeId}",
                        MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID)
        ).andExpect(status().isNotFound())
                .andExpect(res -> Assertions.assertTrue(res.getResolvedException() instanceof ClientExceptionWithBody))
                .andReturn();

        Mockito.verify(merchantServiceMock).getMerchantDetail(anyString(),anyString(), anyString());
    }

    @Test
    void getMerchantList() throws Exception {
        MerchantListDTO dto = MerchantListDTO.builder().content(Collections.emptyList())
                .pageNo(1).pageSize(1).totalElements(1).totalPages(1).build();
        Mockito.when(merchantServiceMock.getMerchantList(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn(dto);

        MvcResult result = mockMvc.perform(
                get("/idpay/merchant/organization/{organizationId}/initiative/{initiativeId}/merchants",
                        ORGANIZATION_ID, INITIATIVE_ID)
                        .param("fiscalCode", FISCAL_CODE)
                        .param("page", String.valueOf(1))
                        .param("size", String.valueOf(10))
        ).andExpect(status().is2xxSuccessful()).andReturn();

        MerchantListDTO resultResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                MerchantListDTO.class);

        Assertions.assertNotNull(resultResponse);
        Assertions.assertEquals(dto,resultResponse);
        Mockito.verify(merchantServiceMock).getMerchantList(anyString(),anyString(), anyString(), any());
    }

    @Test
    void retrieveMerchantIdOK() throws Exception {
        Merchant merchant = MerchantFaker.mockInstance(1);

        Mockito.when(merchantServiceMock.retrieveMerchantId(merchant.getAcquirerId(), merchant.getFiscalCode())).thenReturn(merchant.getMerchantId());

        MvcResult result = mockMvc.perform(
                get("/idpay/merchant/acquirer/{acquirerId}/merchant-fiscalcode/{fiscalCode}/id",  merchant.getAcquirerId(), merchant.getFiscalCode()))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String resultResponse = result.getResponse().getContentAsString();

        Assertions.assertNotNull(resultResponse);
        Assertions.assertEquals(merchant.getMerchantId(), resultResponse);
        Mockito.verify(merchantServiceMock).retrieveMerchantId(anyString(),anyString());
    }

    @Test
    void retrieveMerchantId_NotFoundException() throws Exception {
        Mockito.when(merchantServiceMock.retrieveMerchantId("ACQUIRERID", "FISCALCODE")).thenReturn(null);

        MvcResult result = mockMvc.perform(
                get("/idpay/merchant/acquirer/{acquirerId}/merchant-fiscalcode/{fiscalCode}/id", "ACQUIRERID", "FISCALCODE"))
                .andExpect(status().isNotFound())
                .andExpect(res -> Assertions.assertTrue(res.getResolvedException() instanceof ClientExceptionWithBody))
                .andReturn();

        ErrorDTO errorDTO = objectMapper.readValue(
            result.getResponse().getContentAsString(),
                ErrorDTO.class
        );

        Assertions.assertEquals(MerchantConstants.NOT_FOUND, errorDTO.getCode());
        Assertions.assertEquals(String.format(MerchantConstants.MERCHANTID_BY_ACQUIRERID_AND_FISCALCODE_MESSAGE,"ACQUIRERID" , "FISCALCODE"),errorDTO.getMessage());
        Mockito.verify(merchantServiceMock).retrieveMerchantId(anyString(),anyString());
    }
}