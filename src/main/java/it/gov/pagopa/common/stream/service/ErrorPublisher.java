package it.gov.pagopa.common.stream.service;

import org.springframework.messaging.Message;

public interface ErrorPublisher {
    boolean send(Message<?> message);
}
