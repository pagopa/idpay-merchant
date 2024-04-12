package it.gov.pagopa.merchant.event.consumer;

import it.gov.pagopa.merchant.service.merchant.UploadingMerchantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MerchantFileConsumerConfigTest {
    @Mock
    private UploadingMerchantService uploadingMerchantService;
    @Mock
    Message<String> message;

    @InjectMocks
    private MerchantFileConsumerConfig merchantFileConsumerConfig;

    private Consumer<Message<String>> merchantFileConsumer;

    @BeforeEach
    public void setUp(){
        merchantFileConsumer = merchantFileConsumerConfig.merchantFileConsumer(uploadingMerchantService);
    }

    @Test
    void testConsumerCommands(){
        merchantFileConsumer.accept(message);
        verify(uploadingMerchantService).execute(message);
    }

}