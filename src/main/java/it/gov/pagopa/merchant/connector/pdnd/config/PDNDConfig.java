package it.gov.pagopa.merchant.connector.pdnd.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class PDNDConfig {
    @Value("${pdnd.skipLocalizzazioneNodes:false}")
    private Boolean skipLocalizzazioneNodes;

}