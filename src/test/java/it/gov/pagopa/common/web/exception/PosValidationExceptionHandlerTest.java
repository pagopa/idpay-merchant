package it.gov.pagopa.common.web.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@WebMvcTest(value = {PosValidationExceptionHandlerTest.TestController.class}, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {
        PosValidationExceptionHandlerTest.TestController.class,
        ValidationExceptionHandler.class})
class PosValidationExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoSpyBean
    private TestController testControllerSpy;

    @RestController
    @Slf4j
    static class TestController {

        @PutMapping("/test")
        String testEndpoint(@RequestBody @Valid ValidationDTO body, @RequestHeader("data") String data) {
            return "OK";
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class ValidationDTO {
        @NotBlank(message = "The field is mandatory!")
        private String data;
    }

    private static final ValidationDTO VALIDATION_DTO = new ValidationDTO("data");

    @Test
    void handleMethodArgumentNotValidException() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.put("/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ValidationDTO("")))
                        .header("data", "data")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("[data]: The field is mandatory!"));
    }

    @Test
    void handleMissingRequestHeaderException() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.put("/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(VALIDATION_DTO))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Required request header 'data' for method parameter type String is not present"));

    }

    @Test
    void handlePosValidationExceptions() throws Exception {
        when(testControllerSpy.testEndpoint(Mockito.any(ValidationDTO.class), Mockito.anyString()))
                .thenThrow(new it.gov.pagopa.merchant.exception.custom.PosValidationException(java.util.Collections.emptyList()));

        mockMvc.perform(MockMvcRequestBuilders.put("/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(VALIDATION_DTO))
                        .header("data", "data")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value(it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode.VALIDATION_ERROR))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value(it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionMessage.VALIDATION_ERROR))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.length()").value(0));
    }

    @Test
    void handleMerchantValidationException() throws Exception {
        when(testControllerSpy.testEndpoint(Mockito.any(ValidationDTO.class), Mockito.anyString()))
                .thenThrow(new it.gov.pagopa.merchant.exception.custom.MerchantValidationException(java.util.Collections.emptyList()));

        mockMvc.perform(MockMvcRequestBuilders.put("/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(VALIDATION_DTO))
                        .header("data", "data")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotAcceptable())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code")
                        .value(it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode.VALIDATION_ERROR))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value(it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionMessage.VALIDATION_ERROR))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.length()").value(0));
    }

}