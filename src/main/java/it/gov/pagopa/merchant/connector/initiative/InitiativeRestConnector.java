package it.gov.pagopa.merchant.connector.initiative;

import it.gov.pagopa.merchant.dto.InitiativeDTO;
import org.springframework.web.bind.annotation.PathVariable;

public interface InitiativeRestConnector {
  InitiativeDTO getInitiativeBeneficiaryView(@PathVariable("initiativeId") String initiativeId);
}
