package it.gov.pagopa.merchant.connector.initiative;

import it.gov.pagopa.merchant.dto.initiative.InitiativeDTO;
import org.springframework.web.bind.annotation.PathVariable;

public interface InitiativeRestConnector {
  InitiativeDTO getInitiativeDetail(@PathVariable("initiativeId") String initiativeId);
}
