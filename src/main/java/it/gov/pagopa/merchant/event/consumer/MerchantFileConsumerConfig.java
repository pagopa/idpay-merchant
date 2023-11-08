package it.gov.pagopa.merchant.event.consumer;

import it.gov.pagopa.merchant.service.merchant.UploadingMerchantService;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

@Configuration
public class MerchantFileConsumerConfig {

  @Bean
  public Consumer<Message<String>> merchantFileConsumer(UploadingMerchantService uploadingMerchantService) {
    return uploadingMerchantService::execute;
  }

}
