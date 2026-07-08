package it.gov.pagopa.merchant.dto.pdnd;

import lombok.Data;

import java.util.List;

@Data
public class PdndBusiness {

    private String businessTaxId;
    private String businessName;
    private String legalNature;
    private String legalNatureDescription;
    private String cciaa;
    private String nRea;
    private String vatNumber;
    private String legalForm;
    private String businessStatus;
    private String city;
    private String county;
    private String zipCode;
    private String address;
    private String digitalAddress;
    private List<String> atecoCodes;
    private String disabledStateInstitution;
    private String descriptionStateInstitution;
    private String statusCompanyRI;
    private String statusCompanyRD;

}
