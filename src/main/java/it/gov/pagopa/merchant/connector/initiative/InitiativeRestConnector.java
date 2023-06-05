package it.gov.pagopa.merchant.connector.initiative;

import it.gov.pagopa.merchant.dto.initiative.InitiativeBeneficiaryViewDTO;
import org.springframework.web.bind.annotation.PathVariable;

public interface InitiativeRestConnector {
  InitiativeBeneficiaryViewDTO getInitiativeBeneficiaryView(@PathVariable("initiativeId") String initiativeId);
}
