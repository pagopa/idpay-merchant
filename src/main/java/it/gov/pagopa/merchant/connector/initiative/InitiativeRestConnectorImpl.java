package it.gov.pagopa.merchant.connector.initiative;

import feign.FeignException;
import it.gov.pagopa.merchant.dto.initiative.InitiativeDTO;
import org.springframework.stereotype.Service;

@Service
public class InitiativeRestConnectorImpl implements InitiativeRestConnector {

  private final InitiativeRestClient initiativeRestClient;

  public InitiativeRestConnectorImpl(
      InitiativeRestClient initiativeRestClient) {
    this.initiativeRestClient = initiativeRestClient;
  }

  @Override
  public InitiativeDTO getInitiativeDetail(String initiativeId) {
    try{
      return initiativeRestClient.getInitiativeDetail(initiativeId);
    } catch (FeignException.NotFound e){
      return null;
    }
  }
}
