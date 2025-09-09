package it.gov.pagopa.merchant.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.common.config.JsonConfig;
import it.gov.pagopa.common.web.dto.ErrorDTO;
import it.gov.pagopa.merchant.configuration.MerchantErrorManagerConfig;
import it.gov.pagopa.merchant.configuration.ServiceExceptionConfig;
import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode;
import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionMessage;
import it.gov.pagopa.merchant.dto.MerchantCreateDTO;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.dto.MerchantIbanPatchDTO;
import it.gov.pagopa.merchant.dto.MerchantListDTO;
import it.gov.pagopa.merchant.dto.MerchantUpdateDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.model.Merchant;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantFaker;
import it.gov.pagopa.merchant.test.fakers.MerchantUpdateDTOFaker;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

@WebMvcTest(MerchantControllerImpl.class)
@Import({JsonConfig.class, ServiceExceptionConfig.class, MerchantErrorManagerConfig.class})
class MerchantControllerImplTest {

  @MockitoBean
  private MerchantService merchantServiceMock;

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  private static final String INITIATIVE_ID = "INITIATIVE_ID";
  private static final String ORGANIZATION_ID = "ORGANIZATION_ID";
  private static final String ACQUIRER_ID = "PAGOPA";
  private static final String MERCHANT_ID = "MERCHANT_ID";
  private static final String FISCAL_CODE = "FISCAL_CODE";

  @Test
  void uploadMerchantFile() throws Exception {
    MerchantUpdateDTO merchantUpdateDTO = MerchantUpdateDTOFaker.mockInstance(1);
    merchantUpdateDTO.setStatus("VALIDATED");
    MockMultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
        "content".getBytes());
    Mockito.when(merchantServiceMock.uploadMerchantFile(file, ORGANIZATION_ID, INITIATIVE_ID,
        "ORGANIZATION_USER_ID", ACQUIRER_ID)).thenReturn(merchantUpdateDTO);

    MockMultipartHttpServletRequestBuilder builder = multipart(
        "/idpay/merchant/organization/{organizationId}/initiative/{initiativeId}/upload",
        ORGANIZATION_ID, INITIATIVE_ID);
    builder.header("organization-user-id", "organizationUserId");
    builder.with(
        request -> {
          request.setMethod("PUT");
          return request;
        }
    );

    mockMvc.perform(builder.file(file))
        .andExpect(status().is2xxSuccessful())
        .andDo(print())
        .andReturn();

    Mockito.verify(merchantServiceMock)
        .uploadMerchantFile(Mockito.any(), Mockito.anyString(), Mockito.anyString(),
            Mockito.anyString(), Mockito.anyString());
  }

  @Test
  void getMerchantDetail() throws Exception {
    MerchantDetailDTO dto = MerchantDetailDTOFaker.mockInstance(1);
    Mockito.when(merchantServiceMock.getMerchantDetail(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(dto);

    MvcResult result = mockMvc.perform(
        get("/idpay/merchant/{merchantId}/organization/{organizationId}/initiative/{initiativeId}",
            MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID)
    ).andExpect(status().is2xxSuccessful()).andReturn();

    MerchantDetailDTO resultResponse = objectMapper.readValue(
        result.getResponse().getContentAsString(),
        MerchantDetailDTO.class);

    Assertions.assertNotNull(resultResponse);
    Assertions.assertEquals(dto, resultResponse);
    Mockito.verify(merchantServiceMock).getMerchantDetail(anyString(), anyString(), anyString());
  }

  @Test
  void getMerchantDetail_notFound() throws Exception {
    Mockito.when(merchantServiceMock.getMerchantDetail(Mockito.anyString(), Mockito.anyString(),
            Mockito.anyString()))
        .thenThrow(new MerchantNotFoundException(
            ExceptionCode.MERCHANT_NOT_ONBOARDED,
            String.format(ExceptionMessage.INITIATIVE_AND_MERCHANT_NOT_FOUND, INITIATIVE_ID)));

    mockMvc.perform(
            get("/idpay/merchant/{merchantId}/organization/{organizationId}/initiative/{initiativeId}",
                MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID)
        ).andExpect(status().isNotFound())
        .andExpect(res -> Assertions.assertTrue(
            res.getResolvedException() instanceof MerchantNotFoundException))
        .andReturn();

    Mockito.verify(merchantServiceMock).getMerchantDetail(anyString(), anyString(), anyString());
  }

  @Test
  void getMerchantList() throws Exception {
    MerchantListDTO dto = MerchantListDTO.builder().content(Collections.emptyList())
        .pageNo(1).pageSize(1).totalElements(1).totalPages(1).build();
    Mockito.when(merchantServiceMock.getMerchantList(Mockito.anyString(), Mockito.anyString(),
            Mockito.anyString(), Mockito.any()))
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
    Assertions.assertEquals(dto, resultResponse);
    Mockito.verify(merchantServiceMock)
        .getMerchantList(anyString(), anyString(), anyString(), any());
  }

  @Test
  void getMerchantListWithoutFiscalCode() throws Exception {
    MerchantListDTO dto = MerchantListDTO.builder().content(Collections.emptyList())
        .pageNo(1).pageSize(1).totalElements(1).totalPages(1).build();
    Mockito.when(merchantServiceMock.getMerchantList(Mockito.anyString(), Mockito.anyString(),
            Mockito.isNull(), Mockito.any()))
        .thenReturn(dto);

    MvcResult result = mockMvc.perform(
        get("/idpay/merchant/organization/{organizationId}/initiative/{initiativeId}/merchants",
            ORGANIZATION_ID, INITIATIVE_ID)
            .param("page", String.valueOf(1))
            .param("size", String.valueOf(10))
    ).andExpect(status().is2xxSuccessful()).andReturn();

    MerchantListDTO resultResponse = objectMapper.readValue(
        result.getResponse().getContentAsString(),
        MerchantListDTO.class);

    Assertions.assertNotNull(resultResponse);
    Assertions.assertEquals(dto, resultResponse);
    Mockito.verify(merchantServiceMock).getMerchantList(anyString(), anyString(), isNull(), any());
  }

  @Test
  void retrieveMerchantIdOK() throws Exception {
    Merchant merchant = MerchantFaker.mockInstance(1);

    Mockito.when(
            merchantServiceMock.retrieveMerchantId(merchant.getAcquirerId(), merchant.getFiscalCode()))
        .thenReturn(merchant.getMerchantId());

    MvcResult result = mockMvc.perform(
            get("/idpay/merchant/acquirer/{acquirerId}/merchant-fiscalcode/{fiscalCode}/id",
                merchant.getAcquirerId(), merchant.getFiscalCode()))
        .andExpect(status().is2xxSuccessful())
        .andReturn();

    String resultResponse = result.getResponse().getContentAsString();

    Assertions.assertNotNull(resultResponse);
    Assertions.assertEquals(merchant.getMerchantId(), resultResponse);
    Mockito.verify(merchantServiceMock).retrieveMerchantId(anyString(), anyString());
  }

  @Test
  void retrieveMerchantId_NotFoundException() throws Exception {
    Mockito.when(merchantServiceMock.retrieveMerchantId("ACQUIRERID", "FISCALCODE"))
        .thenReturn(null);

    MvcResult result = mockMvc.perform(
            get("/idpay/merchant/acquirer/{acquirerId}/merchant-fiscalcode/{fiscalCode}/id",
                "ACQUIRERID", "FISCALCODE"))
        .andExpect(status().isNotFound())
        .andExpect(res -> Assertions.assertTrue(
            res.getResolvedException() instanceof MerchantNotFoundException))
        .andReturn();

    ErrorDTO errorDTO = objectMapper.readValue(
        result.getResponse().getContentAsString(),
        ErrorDTO.class
    );

    Assertions.assertEquals(ExceptionCode.MERCHANT_NOT_FOUND, errorDTO.getCode());
    Assertions.assertEquals(ExceptionMessage.MERCHANT_NOT_FOUND_MESSAGE, errorDTO.getMessage());
    Mockito.verify(merchantServiceMock).retrieveMerchantId(anyString(), anyString());
  }

  @Test
  void updateIban() throws Exception {
    MerchantIbanPatchDTO requestDto = new MerchantIbanPatchDTO();
    requestDto.setIban("IT60X0542811101000000123456");

    MerchantDetailDTO responseDto = MerchantDetailDTOFaker.mockInstance(1);
    Mockito.when(merchantServiceMock.updateIban(
        anyString(), anyString(), anyString(), any(MerchantIbanPatchDTO.class))
    ).thenReturn(responseDto);

    mockMvc.perform(
            patch(
                "/idpay/merchant/{merchantId}/organization/{organizationId}/initiative/{initiativeId}",
                MERCHANT_ID, ORGANIZATION_ID, INITIATIVE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        )
        .andExpect(status().isOk());
    Mockito.verify(merchantServiceMock)
        .updateIban(anyString(), anyString(), anyString(), any(MerchantIbanPatchDTO.class));
  }


  @Test
  void createOrUpdateMerchant_ok() throws Exception {
    String acquirerId = "ACQ123";
    String businessName = "Test Business";
    String fiscalCode = "ABCDEF12G34H567I";
    String iban = "IT60X0542811101000000123456";
    String ibanHolder = "Test Iban Holder";
    String expectedMerchantId = "MERCHANT123";

    MerchantCreateDTO dto = MerchantCreateDTO.builder()
        .businessName(businessName)
        .fiscalCode(fiscalCode)
        .acquirerId(acquirerId)
        .iban(iban)
        .ibanHolder(ibanHolder)
        .build();

    Mockito.when(merchantServiceMock.retrieveOrCreateMerchantIfNotExists(
            dto))
        .thenReturn(expectedMerchantId);

    mockMvc.perform(
            put("/idpay/merchant")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().string(expectedMerchantId));
  }

  @Test
  void createMerchant_Ko_OrUpdate_MerchantAlreadyExist() throws Exception {
    String acquirerId = "ACQ123";
    String businessName = "Test Business";
    String fiscalCode = "ABCDEF12G34H567I";
    String expectedMerchantId = "MERCHANT123";
    String iban = "IT60X0542811101000000123456";
    String ibanHolder = "Test Iban Holder";

    MerchantCreateDTO dto = MerchantCreateDTO.builder()
        .businessName(businessName)
        .fiscalCode(fiscalCode)
        .acquirerId(acquirerId)
        .iban(iban)
        .ibanHolder(ibanHolder)
        .build();


    Mockito.when(merchantServiceMock.retrieveOrCreateMerchantIfNotExists(
            dto))
        .thenReturn(expectedMerchantId);

    mockMvc.perform(
            put("/idpay/merchant")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(content().string(expectedMerchantId));
  }

  @Test
  void createOrUpdateMerchant_Ko_GenericException() throws Exception {
    String acquirerId = "ACQ123";
    String businessName = "Test Business";
    String fiscalCode = "ABCDEF12G34H567I";

    MerchantCreateDTO dto = MerchantCreateDTO.builder()
        .businessName(businessName)
        .fiscalCode(fiscalCode)
        .acquirerId(acquirerId).build();

    final String genericException = "Generic Exception";

    Mockito.when(merchantServiceMock.retrieveOrCreateMerchantIfNotExists(
            dto))
        .thenThrow(new RuntimeException(genericException));

    mockMvc.perform(
            put("/idpay/merchant")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value(ExceptionCode.GENERIC_ERROR))
        .andExpect(jsonPath("$.message").value("A generic error occurred"
        ));
  }

  @Test
  void createOrUpdateMerchant_Ko_MissingMandatoryParams() throws Exception {
    String acquirerId = "ACQ123";
    String fiscalCode = "ABCDEF12G34H567I";
    String expectedMerchantId = "MERCHANT123";

    MerchantCreateDTO dto = MerchantCreateDTO.builder()
        .fiscalCode(fiscalCode)
        .acquirerId(acquirerId).build();

    Mockito.when(merchantServiceMock.retrieveOrCreateMerchantIfNotExists(
            dto))
        .thenReturn(expectedMerchantId);

    mockMvc.perform(
            put("/idpay/merchant")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest());
  }

}
