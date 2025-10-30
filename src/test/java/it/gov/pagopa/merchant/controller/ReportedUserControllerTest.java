package it.gov.pagopa.merchant.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.merchant.dto.ReportedUserCreateResponseDTO;
import it.gov.pagopa.merchant.dto.ReportedUserDTO;
import it.gov.pagopa.merchant.dto.ReportedUserRequestDTO;
import it.gov.pagopa.merchant.service.ReportedUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReportedUserControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ReportedUserService reportedUserService;

    @BeforeEach
    void setup() {
        ReportedUserController controller = new ReportedUserController(reportedUserService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void create_shouldCallServiceAndReturnBody() throws Exception {
        ReportedUserRequestDTO req =
                new ReportedUserRequestDTO("MERCH-1", "INIT-1", "RSSMRA80A01H501U");
        ReportedUserCreateResponseDTO serviceRes = ReportedUserCreateResponseDTO.ok();

        when(reportedUserService.createReportedUser(any(ReportedUserRequestDTO.class)))
                .thenReturn(serviceRes);

        MvcResult result = mockMvc.perform(post("/idpay/merchant/reportedUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());

        JsonNode expected = objectMapper.readTree(objectMapper.writeValueAsString(serviceRes));
        JsonNode actual   = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(expected, actual);

        verify(reportedUserService).createReportedUser(refEq(req));
    }

    @Test
    void search_shouldMapQueryParamsAndReturnList() throws Exception {
        String merchantId = "MERCH-1";
        String initiativeId = "INIT-1";
        String fc = "RSSMRA80A01H501U";

        List<ReportedUserDTO> out = List.of(
                ReportedUserDTO.builder().transactionId("T1").fiscalCode(fc).build(),
                ReportedUserDTO.builder().transactionId("T2").fiscalCode(fc).build()
        );

        when(reportedUserService.searchReportedUser(any(ReportedUserRequestDTO.class)))
                .thenReturn(out);

        MvcResult result = mockMvc.perform(get("/idpay/merchant/reportedUser")
                        .param("merchantId", merchantId)
                        .param("initiativeId", initiativeId)
                        .param("userFiscalCode", fc))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());

        JsonNode expected = objectMapper.readTree(objectMapper.writeValueAsString(out));
        JsonNode actual   = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(expected, actual);


        ArgumentCaptor<ReportedUserRequestDTO> captor = ArgumentCaptor.forClass(ReportedUserRequestDTO.class);
        verify(reportedUserService).searchReportedUser(captor.capture());
        ReportedUserRequestDTO passed = captor.getValue();
        assertEquals(merchantId,  passed.getMerchantId());
        assertEquals(fc,          passed.getInitiativeId());
        assertEquals(initiativeId, passed.getUserFiscalCode());
    }

    @Test
    void delete_shouldCallServiceAndReturnBody() throws Exception {

        String req = "RSSMRA80A01H501U";
        ReportedUserCreateResponseDTO serviceRes = ReportedUserCreateResponseDTO.ok();

        given(reportedUserService.deleteByUserId(req)).willReturn(serviceRes);


        MvcResult result = mockMvc.perform(
                        delete("/idpay/merchant/reportedUser")
                                .param("userFiscalCode", req)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode expected = objectMapper.valueToTree(serviceRes);
        JsonNode actual   = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(expected, actual, "Response body JSON should match");

        verify(reportedUserService).deleteByUserId(req);
    }
}
