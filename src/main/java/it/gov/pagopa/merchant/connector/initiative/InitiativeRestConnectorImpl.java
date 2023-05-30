package it.gov.pagopa.merchant.connector.initiative;

import it.gov.pagopa.merchant.dto.initiative.InitiativeBeneficiaryViewDTO;
import org.springframework.stereotype.Service;

@Service
public class InitiativeRestConnectorImpl implements InitiativeRestConnector {

  private final InitiativeRestClient initiativeRestClient;

  public InitiativeRestConnectorImpl(
      InitiativeRestClient initiativeRestClient) {
    this.initiativeRestClient = initiativeRestClient;
  }

  @Override
  public InitiativeBeneficiaryViewDTO getInitiativeBeneficiaryView(String initiativeId) {
    return initiativeRestClient.getInitiativeBeneficiaryView(initiativeId);
  }
}
