package it.gov.pagopa.merchant.configuration.pdnd;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class PdndConfig {
    @Value("${pdnd.skipLocalizzazioneNodes:false}")
    private Boolean skipLocalizzazioneNodes;

}