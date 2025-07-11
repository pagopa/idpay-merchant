package it.gov.pagopa.merchant.configuration;

import it.gov.pagopa.common.web.dto.ErrorDTO;
import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PointOfSaleErrorManagerConfig {

  @Bean
  ErrorDTO defaultErrorDTO() {
    return new ErrorDTO(
        ExceptionCode.GENERIC_ERROR,
        "A generic error occurred"
    );
  }

  @Bean
  ErrorDTO tooManyRequestsErrorDTO() {
    return new ErrorDTO(ExceptionCode.TOO_MANY_REQUESTS, "Too Many Requests");
  }

  @Bean
  ErrorDTO templateValidationErrorDTO(){
    return new ErrorDTO(ExceptionCode.INVALID_REQUEST, null);
  }
}
