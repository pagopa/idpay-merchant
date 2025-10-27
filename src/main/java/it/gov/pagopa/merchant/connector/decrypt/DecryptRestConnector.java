package it.gov.pagopa.merchant.connector.decrypt;

import it.gov.pagopa.merchant.dto.DecryptCfDTO;
import org.springframework.stereotype.Service;

@Service
public interface DecryptRestConnector {

  DecryptCfDTO getPiiByToken(String token);
}
