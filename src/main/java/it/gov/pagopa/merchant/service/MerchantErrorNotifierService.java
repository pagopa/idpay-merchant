package it.gov.pagopa.merchant.service;

import org.springframework.messaging.Message;

public interface MerchantErrorNotifierService {

  void notifyMerchantFileUpload(Message<?> message, String description, boolean retryable, Throwable exception);

  @SuppressWarnings("squid:S00107") // suppressing too many parameters alert
  void notify(String srcType, String srcServer, String srcTopic, String group, Message<?> message, String description, boolean retryable, boolean resendApplication, Throwable exception);

}
