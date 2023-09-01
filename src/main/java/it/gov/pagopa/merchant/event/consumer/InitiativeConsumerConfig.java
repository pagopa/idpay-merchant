package it.gov.pagopa.merchant.event.consumer;

import it.gov.pagopa.merchant.dto.QueueInitiativeDTO;
import it.gov.pagopa.merchant.service.MerchantService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class InitiativeConsumerConfig {

    @Bean
    public Consumer<QueueInitiativeDTO> initiativeConsumer(MerchantService merchantService){
        return merchantService::updatingInitiative;
    }
}
