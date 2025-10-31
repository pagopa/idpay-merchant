package it.gov.pagopa.merchant.connector.encrypt;

import it.gov.pagopa.merchant.dto.CFDTO;
import it.gov.pagopa.merchant.dto.EncryptedCfDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public interface EncryptRestConnector {

  EncryptedCfDTO upsertToken(@RequestBody CFDTO body);
}
