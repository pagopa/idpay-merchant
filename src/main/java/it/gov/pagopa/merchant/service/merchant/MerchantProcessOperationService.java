package it.gov.pagopa.merchant.service.merchant;

import it.gov.pagopa.merchant.dto.QueueCommandOperationDTO;

public interface MerchantProcessOperationService {
    void processOperation(QueueCommandOperationDTO queueCommandOperationDTO);
}
