package it.gov.pagopa.merchant.dto.pdnd;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Localizzazioni {

 @JacksonXmlElementWrapper(useWrapping = false)
 @JacksonXmlProperty(localName = "localizzazione")
 private List<LocalizzazioneWrapper> localizzazioni;

}
