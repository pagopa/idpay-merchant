package it.gov.pagopa.merchant.service;

import it.gov.pagopa.common.kafka.service.ErrorNotifierService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class MerchantErrorNotifierServiceImpl implements MerchantErrorNotifierService {

  private final ErrorNotifierService errorNotifierService;
  private final String merchantFileUploadMessagingServiceType;
  private final String merchantFileUploadServer;
  private final String merchantFileUploadTopic;
  private final String merchantFileUploadGroup;

  public MerchantErrorNotifierServiceImpl(ErrorNotifierService errorNotifierService,
                                          @Value("${spring.cloud.stream.binders.kafka-reward-notification-upload.type}") String merchantFileUploadMessagingServiceType,
                                          @Value("${spring.cloud.stream.binders.kafka-reward-notification-upload.environment.spring.cloud.stream.kafka.binder.brokers}") String merchantFileUploadServer,
                                          @Value("${spring.cloud.stream.bindings.merchantFileConsumer-in-0.destination}") String merchantFileUploadTopic,
                                          @Value("${spring.cloud.stream.bindings.merchantFileConsumer-in-0.group}") String merchantFileUploadGroup) {
    this.errorNotifierService = errorNotifierService;
    this.merchantFileUploadMessagingServiceType = merchantFileUploadMessagingServiceType;
    this.merchantFileUploadServer = merchantFileUploadServer;
    this.merchantFileUploadTopic = merchantFileUploadTopic;
    this.merchantFileUploadGroup = merchantFileUploadGroup;
  }

  @Override
  public void notifyMerchantFileUpload(Message<?> message, String description, boolean retryable, Throwable exception) {
    notify(merchantFileUploadMessagingServiceType, merchantFileUploadServer, merchantFileUploadTopic, merchantFileUploadGroup, message, description, retryable, false, exception);
  }

  @Override
  public void notify(String srcType, String srcServer, String srcTopic, String group, Message<?> message, String description, boolean retryable, boolean resendApplication, Throwable exception) {
    errorNotifierService.notify(srcType, srcServer, srcTopic, group, message, description, retryable, resendApplication, exception);
  }
}
