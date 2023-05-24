package it.gov.pagopa.merchant.event.consumer;

import it.gov.pagopa.merchant.dto.StorageEventDTO;
import it.gov.pagopa.merchant.service.merchant.UploadingMerchantService;

import java.util.List;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MerchantFileConsumer {

  @Bean
  public Consumer<List<StorageEventDTO>> merchantFileConsumer(UploadingMerchantService uploadingMerchantService) {
    return uploadingMerchantService::ingestionMerchantFile;
  }

}
