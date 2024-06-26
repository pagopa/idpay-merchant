package it.gov.pagopa.merchant.event.consumer;

import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.QueueInitiativeDTO;
import it.gov.pagopa.merchant.service.MerchantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.function.Consumer;
@ExtendWith(MockitoExtension.class)
class InitiativeConsumerConfigTest {
    @Mock
    private MerchantService merchantService;
    @InjectMocks
    private InitiativeConsumerConfig initiativeConsumerConfig;

    private Consumer<QueueInitiativeDTO> initiativeConsumer;
    private final static LocalDateTime OPERATION_TIME = LocalDateTime.now();
    private final static String INITIATIVEID = "INITIATIVEID";
    private final static String STATUS = "STATUS";
    private final static String REWARDTYPE = "REWARDTYPE";

    @BeforeEach
    public void setUp(){
        initiativeConsumer = initiativeConsumerConfig.initiativeConsumer(merchantService);
    }

    @Test
    void testInitiativeConsumer(){
        QueueInitiativeDTO initiativeDTO = new QueueInitiativeDTO(INITIATIVEID,STATUS,OPERATION_TIME,REWARDTYPE);
        initiativeDTO.setInitiativeRewardType("DISCOUNT");
        initiativeDTO.setStatus(MerchantConstants.INITIATIVE_PUBLISHED);

        initiativeConsumer.accept(initiativeDTO);

        Mockito.verify(merchantService).updatingInitiative(initiativeDTO);
    }

}