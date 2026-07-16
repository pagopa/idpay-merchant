package it.gov.pagopa.merchant.connector.pdnd;

import java.util.List;

public interface PdndInfoCamereConnector {

    List<String> retrieveAtecoCodes(String taxCode, List<String> currentAtecoCodes);
}
