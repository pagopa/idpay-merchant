package it.gov.pagopa.merchant.dto.pdnd;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PdndImpresa {

  @JsonProperty("ProgressivoImpresa")
  private long progressivoImpresa;

  @JsonProperty("CodiceFiscale")
  private String businessTaxId;

  @JsonProperty("Denominazione")
  private String businessName;

  @JsonProperty("NaturaGiuridica")
  private String legalNature;

  @JsonProperty("DescNaturaGiuridica")
  private String legalNatureDescription;

  @JsonProperty("Cciaa")
  private String cciaa;

  @JsonProperty("NRea")
  private String nRea;

  @JsonProperty("StatoImpresa")
  @JsonAlias("StatoDitta")
  private String businessStatus;

  @JsonProperty("IndirizzoSedeLegale")
  private PdndSedeImpresa businessAddress;

  @JsonProperty("PEC")
  private String digitalAddress;

  public String getAddress() {
    if (Objects.nonNull(businessAddress)) {
      return businessAddress.getToponimoSede()
          + " "
          + businessAddress.getViaSede()
          + " "
          + businessAddress.getNcivicoSede();
    } else return "";
  }
}
