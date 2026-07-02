package it.gov.pagopa.merchant.connector.pdnd.connector;

import it.gov.pagopa.merchant.connector.pdnd.dto.PDNDBusiness;

import java.util.List;

public interface PDNDInfoCamereConnector {
    /*
    List<PDNDBusiness> retrieveInstitutionsPdndByDescription(String description);
    PDNDBusiness retrieveInstitutionPdndByTaxCode(String taxCode);
    byte[] retrieveInstitutionDocument(String taxCode);
    PDNDBusiness retrieveInstitutionFromRea(String county, String rea);
    PDNDBusiness retrieveInstitutionDetail(String taxCode);
    */

    List<String> retrieveAtecoCodes(String taxCode);
}
