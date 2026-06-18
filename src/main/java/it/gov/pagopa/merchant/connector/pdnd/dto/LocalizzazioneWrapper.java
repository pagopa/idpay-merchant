package it.gov.pagopa.merchant.connector.pdnd.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import it.gov.pagopa.merchant.connector.pdnd.dto.ClassificazioniAteco;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalizzazioneWrapper {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "indirizzo_localizzazione")
    private it.gov.pagopa.merchant.connector.pdnd.dto.Localizzazione Localizzazione;

    @JacksonXmlProperty(localName = "classificazioni-ateco")
    private ClassificazioniAteco classificazioniAteco;

}