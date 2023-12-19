package it.gov.pagopa.merchant.configuration;

import it.gov.pagopa.merchant.constants.MerchantConstants.ExceptionCode;
import it.gov.pagopa.common.web.dto.ErrorDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MerchantErrorManagerConfig {

  @Bean
  ErrorDTO defaultErrorDTO() {
    return new ErrorDTO(
        ExceptionCode.GENERIC_ERROR,
        "A generic error occurred for payment"
    );
  }

  @Bean
  ErrorDTO tooManyRequestsErrorDTO() {
    return new ErrorDTO(ExceptionCode.TOO_MANY_REQUESTS, "Too Many Requests");
  }
}
