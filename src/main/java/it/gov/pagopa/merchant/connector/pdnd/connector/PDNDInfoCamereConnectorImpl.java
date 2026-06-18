package it.gov.pagopa.merchant.connector.pdnd.connector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import it.gov.pagopa.merchant.connector.pdnd.config.PDNDConfig;
import it.gov.pagopa.merchant.connector.pdnd.config.PDNDInfoCamereRestClientConfig;
import it.gov.pagopa.merchant.connector.pdnd.config.PDNDVisuraInfoCamereRestClientConfig;
import it.gov.pagopa.merchant.connector.pdnd.dto.ClientCredentialsResponse;
import it.gov.pagopa.merchant.connector.pdnd.dto.PDNDBusiness;
import it.gov.pagopa.merchant.connector.pdnd.dto.PDNDImpresa;
import it.gov.pagopa.merchant.connector.pdnd.dto.PDNDVisuraImpresa;
import it.gov.pagopa.merchant.connector.pdnd.exception.ResourceNotFoundException;
import it.gov.pagopa.merchant.connector.pdnd.extra.rest.PDNDVisuraInfoCamereRestClient;
import it.gov.pagopa.merchant.connector.pdnd.mapper.PDNDBusinessMapper;
import it.gov.pagopa.merchant.connector.pdnd.extra.rest.PDNDInfoCamereRestClient;
import it.gov.pagopa.merchant.connector.pdnd.rest.PDNDVisuraInfoCamereRawRestClient;
import it.gov.pagopa.merchant.connector.pdnd.service.PDNDCacheableService;
import it.gov.pagopa.merchant.connector.pdnd.service.TokenProvider;
import it.gov.pagopa.merchant.connector.pdnd.service.TokenProviderPDND;
import it.gov.pagopa.merchant.connector.pdnd.service.TokenProviderVisura;
import it.gov.pagopa.merchant.connector.pdnd.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
@Slf4j
@Service
public class PDNDInfoCamereConnectorImpl implements PDNDInfoCamereConnector {
  private static final String TAX_CODE_REQUIRED_MESSAGE = "TaxCode is required";

  private final PDNDBusinessMapper pdndBusinessMapper;
  private final PDNDCacheableService PDNDCacheableService;
  private final PDNDConfig pdndConfig;

  /*
  private final PDNDInfoCamereRestClient pdndInfoCamereRestClient;
  private final PDNDVisuraInfoCamereRawRestClient pdndVisuraInfoCamereRawRestClient;
  private final PDNDVisuraInfoCamereRestClient pdndVisuraInfoCamereRestClient;
  private final TokenProvider tokenProviderPDND;
  private final TokenProviderVisura tokenProviderVisura;
  private final PDNDInfoCamereRestClientConfig pdndInfoCamereRestClientConfig;
  private final PDNDVisuraInfoCamereRestClientConfig pdndVisuraInfoCamereRestClientConfig;
  private static final String BEARER = "Bearer ";
  */

  public PDNDInfoCamereConnectorImpl(
          /*
          PDNDInfoCamereRestClient pdndInfoCamereRestClient,
          PDNDVisuraInfoCamereRawRestClient pdndVisuraInfoCamereRawRestClient,
          PDNDVisuraInfoCamereRestClient pdndVisuraInfoCamereRestClient,
          TokenProviderPDND tokenProviderPDND,
          TokenProviderVisura tokenProviderVisura,
          PDNDInfoCamereRestClientConfig pdndInfoCamereRestClientConfig,
          PDNDVisuraInfoCamereRestClientConfig pdndVisuraInfoCamereRestClientConfig,
          */
          PDNDBusinessMapper pdndBusinessMapper,
          PDNDCacheableService PDNDCacheableService,
          PDNDConfig pdndConfig) {
    /*
    this.pdndInfoCamereRestClient = pdndInfoCamereRestClient;
    this.pdndVisuraInfoCamereRawRestClient = pdndVisuraInfoCamereRawRestClient;
    this.pdndVisuraInfoCamereRestClient = pdndVisuraInfoCamereRestClient;
    this.tokenProviderPDND = tokenProviderPDND;
    this.tokenProviderVisura = tokenProviderVisura;
    this.pdndInfoCamereRestClientConfig = pdndInfoCamereRestClientConfig;
    this.pdndVisuraInfoCamereRestClientConfig = pdndVisuraInfoCamereRestClientConfig;
    */
    this.pdndBusinessMapper = pdndBusinessMapper;
    this.PDNDCacheableService = PDNDCacheableService;
    this.pdndConfig = pdndConfig;
  }


  /*
  Metodo di interesse
   */
  @Override
  public PDNDBusiness retrieveInstitutionDetail(String taxCode) {
      Assert.hasText(taxCode, TAX_CODE_REQUIRED_MESSAGE);
        String encTaxCode = DataEncryptionUtils.encrypt(taxCode);
      try {
        String document = PDNDCacheableService.getEncryptedDocument(encTaxCode);

        String decDocument = DataEncryptionUtils.decrypt(document);

        /*
          storageService.saveStringToStorage(document,"visura_" + taxCode + "_" + LocalDateTime.now() + ".xml");
        */

        PDNDVisuraImpresa result = xmlToVisuraImpresa(decDocument.getBytes(StandardCharsets.UTF_8));

       return pdndBusinessMapper.toPDNDBusiness(result, pdndConfig);

      } catch (Exception e) {
        log.error("Unexpected exception occurred while retrieving institution detail", e);
        throw new IllegalArgumentException("Unexpected error while retrieving institution detail", e);
      }
  }

  private PDNDVisuraImpresa xmlToVisuraImpresa(byte[] xmlBytes) throws IOException {
    XmlMapper xmlMapper = new XmlMapper();
    xmlMapper.findAndRegisterModules();
    return xmlMapper.readValue(xmlBytes, PDNDVisuraImpresa.class);
  }


  /*

    @Override
    public byte[] retrieveInstitutionDocument(String taxCode) {
      Assert.hasText(taxCode, TAX_CODE_REQUIRED_MESSAGE);
      ClientCredentialsResponse tokenResponse = tokenProviderVisura.getTokenPdnd(pdndVisuraInfoCamereRestClientConfig.getPdndSecretValue());
      String bearer = BEARER + tokenResponse.getAccessToken();
      byte[] result = pdndVisuraInfoCamereRawRestClient.getRawInstitutionDetail(taxCode, bearer);
      try {
        return XMLCleaner.cleanXml(result, Arrays.asList("persone-sede", "elenco-soci"));
      } catch (Exception e) {
        throw new IllegalArgumentException("Impossible to parse document for institution with taxCode: " + taxCode);
      }
    }
    @Override
    public PDNDBusiness retrieveInstitutionFromRea(String county, String rea) {
      Assert.hasText(rea, "Rea is required");
      Assert.hasText(rea, "County is required");
      ClientCredentialsResponse tokenResponse = tokenProviderVisura.getTokenPdnd(pdndVisuraInfoCamereRestClientConfig.getPdndSecretValue());
      String bearer = BEARER + tokenResponse.getAccessToken();
      List<PDNDImpresa> institutions = pdndVisuraInfoCamereRestClient.retrieveInstitutionPdndFromRea(rea, county, bearer);
      if (Objects.isNull(institutions) || institutions.isEmpty()) {
        throw new ResourceNotFoundException("No institution found with rea: " + county + "-" + rea);
      }
      PDNDImpresa result  = institutions.get(0);
      PDNDVisuraImpresa visuraImpresa = pdndVisuraInfoCamereRestClient.retrieveInstitutionDetail(result.getBusinessTaxId(), bearer);
      return pdndBusinessMapper.toPDNDBusiness(visuraImpresa, pdndConfig);
    }

    @Override
    public List<PDNDBusiness> retrieveInstitutionsPdndByDescription(String description) {
      Assert.hasText(description, "Description is required");
      ClientCredentialsResponse tokenResponse = tokenProviderPDND.getTokenPdnd(pdndInfoCamereRestClientConfig.getPdndSecretValue());
      String bearer = BEARER + tokenResponse.getAccessToken();
      List<PDNDImpresa> result = pdndInfoCamereRestClient.retrieveInstitutionsPdndByDescription(description, bearer);
      return pdndBusinessMapper.toPDNDBusinesses(result);
    }

    @Override
    public PDNDBusiness retrieveInstitutionPdndByTaxCode(String taxCode) {
      Assert.hasText(taxCode, TAX_CODE_REQUIRED_MESSAGE);
      String encTaxCode = DataEncryptionUtils.encrypt(taxCode);
      PDNDImpresa impresa = null;

      try {
        String encResult = PDNDCacheableService.getEncryptedPDNDImpresa(encTaxCode);
        String decResult = DataEncryptionUtils.decrypt(encResult);

        impresa = new ObjectMapper().readValue(decResult, new TypeReference<>(){
        });
      } catch (Exception e) {
        log.error("Errore", e);
      }
      return pdndBusinessMapper.toPDNDBusiness(impresa);
    }

  */

}
