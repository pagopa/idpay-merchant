package it.gov.pagopa.merchant.dto.pdnd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PdndSedeImpresa {
  @JsonProperty("ComuneSede")
  private String city;

  @JsonProperty("ProvinciaSede")
  private String county;

  @JsonProperty("CapSede")
  private String zipCode;

  @JsonProperty("ToponimoSede")
  private String toponimoSede;

  @JsonProperty("ViaSede")
  private String viaSede;

  @JsonProperty("NcivicoSede")
  private String ncivicoSede;
}
