package it.gov.pagopa.merchant.service;

import it.gov.pagopa.common.kafka.service.ErrorNotifierService;
import it.gov.pagopa.common.utils.TestUtils;
import it.gov.pagopa.merchant.dto.StorageEventDTO;
import it.gov.pagopa.merchant.test.fakers.StorageEventDTOFaker;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MerchantErrorNotifierServiceTest {

  public static final String MESSAGING_SERVICE_TYPE = "MessagingServiceType";
  public static final String SERVER = "srcServer";
  public static final String TOPIC = "srcTopic";
  public static final String GROUP = "group";
  public static final String ERROR_MESSAGE = "test";
  @Mock
  private ErrorNotifierService errorNotifierServiceMock;

  private MerchantErrorNotifierService merchantErrorNotifierService;

  @BeforeEach
  void setUp() {
    merchantErrorNotifierService = new MerchantErrorNotifierServiceImpl(
        errorNotifierServiceMock, MESSAGING_SERVICE_TYPE, SERVER, TOPIC, GROUP);
  }

  @Test
  void notifyMerchantFileUpload(){

    when(errorNotifierServiceMock.notify(any(), any(), any(), any(), any(), any(), anyBoolean(), anyBoolean(), any())).thenReturn(false);

    merchantErrorNotifierService.notifyMerchantFileUpload(buildMessage(), "", true, new Throwable(ERROR_MESSAGE));

    verify(errorNotifierServiceMock).notify(any(), any(), any(), any(), any(), any(), anyBoolean(), anyBoolean(), any());
  }

  @NotNull
  private Message<String> buildMessage() {
    StorageEventDTO storageEventDTO = StorageEventDTOFaker.mockInstance(1);
    List<StorageEventDTO> storageEventDTOS = List.of(storageEventDTO);
    return MessageBuilder.withPayload(TestUtils.jsonSerializer(storageEventDTOS)).build();
  }
}
