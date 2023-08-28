package it.gov.pagopa.merchant.event.consumer;

import it.gov.pagopa.merchant.dto.QueueCommandOperationDTO;
import it.gov.pagopa.merchant.service.MerchantService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class CommandsConsumer {
    @Bean
    public Consumer<QueueCommandOperationDTO> consumerCommands(MerchantService merchantService) {
        return merchantService::processOperation;
    }
}
