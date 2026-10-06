package it.gov.pagopa.merchant.connector.pdnd;

import it.gov.pagopa.merchant.service.pdnd.PdndCacheableService;
import it.gov.pagopa.merchant.utils.DataEncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
@Service
public class PdndInfoCamereConnectorImpl implements PdndInfoCamereConnector {
  private static final String TAX_CODE_REQUIRED_MESSAGE = "TaxCode is required";
  private final PdndCacheableService pdndCacheableService;

  public PdndInfoCamereConnectorImpl(
          PdndCacheableService pdndCacheableService) {
    this.pdndCacheableService = pdndCacheableService;
  }

  @Override
  public List<String> retrieveAtecoCodes(String fiscalCode, List<String> currentAtecoCodes) {
    Assert.hasText(fiscalCode, TAX_CODE_REQUIRED_MESSAGE);
    String encFiscalCode = DataEncryptionUtils.encrypt(fiscalCode);
    return pdndCacheableService.getAtecoCodes(encFiscalCode, currentAtecoCodes);
  }

}
