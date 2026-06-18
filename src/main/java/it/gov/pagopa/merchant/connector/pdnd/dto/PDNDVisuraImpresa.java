package it.gov.pagopa.merchant.connector.pdnd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import it.gov.pagopa.merchant.connector.pdnd.dto.DatiIdentificativiImpresa;
import it.gov.pagopa.merchant.connector.pdnd.dto.InfoAttivita;
import it.gov.pagopa.merchant.connector.pdnd.dto.Localizzazioni;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PDNDVisuraImpresa {

  @JsonProperty("dati-identificativi")
  private DatiIdentificativiImpresa datiIdentificativiImpresa;

  @JsonProperty("info-attivita")
  private InfoAttivita infoAttivita;

  @JsonProperty("localizzazioni")
  private Localizzazioni pointOfSales;



}
