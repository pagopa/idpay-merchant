package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.model.Initiative;
import org.apache.commons.lang3.BooleanUtils;

import java.util.List;

public interface MerchantService {
    List<Initiative> getMerchantInitiativeList(String merchantId, Boolean enabled);
}
