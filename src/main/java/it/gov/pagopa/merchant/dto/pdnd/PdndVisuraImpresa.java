package it.gov.pagopa.merchant.dto.pdnd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PdndVisuraImpresa {

  @JsonProperty("dati-identificativi")
  private DatiIdentificativiImpresa datiIdentificativiImpresa;

  @JsonProperty("info-attivita")
  private InfoAttivita infoAttivita;

  @JsonProperty("localizzazioni")
  private Localizzazioni pointOfSales;
  }
