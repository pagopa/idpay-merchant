package it.gov.pagopa.merchant.connector.payment.dto;

public enum SyncTrxStatus {
  CREATED,
  IDENTIFIED,
  AUTHORIZATION_REQUESTED,
  AUTHORIZED,
  CAPTURED,
  REWARDED,
  REJECTED,
  CANCELLED,
  REFUNDED
}
