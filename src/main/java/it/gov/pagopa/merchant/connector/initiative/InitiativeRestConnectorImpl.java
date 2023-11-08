package it.gov.pagopa.merchant.connector.initiative;

import feign.FeignException;
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
    try{
      return initiativeRestClient.getInitiativeBeneficiaryView(initiativeId);
    } catch (FeignException.NotFound e){
      return null;
    }
  }
}
