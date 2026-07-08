package it.gov.pagopa.merchant.mapper;


import it.gov.pagopa.merchant.configuration.pdnd.PdndConfig;
import it.gov.pagopa.merchant.dto.pdnd.PdndVisuraImpresa;
import it.gov.pagopa.merchant.dto.pdnd.ClassificazioneAteco;
import it.gov.pagopa.merchant.dto.pdnd.Localizzazione;
import it.gov.pagopa.merchant.dto.pdnd.PdndBusiness;
import it.gov.pagopa.merchant.dto.pdnd.PdndImpresa;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Component
public class PdndBusinessMapper {

    public PdndBusiness toPDNDBusiness(PdndImpresa src) {
        if (src == null) return null;

        PdndBusiness target = new PdndBusiness();

        if (src.getBusinessAddress() != null) {
            target.setCity(src.getBusinessAddress().getCity());
            target.setCounty(src.getBusinessAddress().getCounty());
            target.setZipCode(src.getBusinessAddress().getZipCode());
        }

        target.setDigitalAddress(src.getDigitalAddress());

        return target;
    }

    public PdndBusiness toPDNDBusiness(PdndVisuraImpresa src, PdndConfig config) {
        if (src == null) return null;

        PdndBusiness target = new PdndBusiness();

        var dati = src.getDatiIdentificativiImpresa();
        if (dati != null) {
            var loc = dati.getLocalizzazione();

            if (loc != null) {
                target.setCity(loc.getComune());
                target.setCounty(loc.getProvincia());
                target.setZipCode(loc.getCap());
                target.setAddress(mapAddress(loc));
            }

            target.setVatNumber(dati.getVatNumber());
            target.setLegalForm(dati.getLegalForm());
            target.setStatusCompanyRI(dati.getStatusCompanyRI());
            target.setStatusCompanyRD(dati.getStatusCompanyRD());
        }

        target.setAtecoCodes(mapAtecoCodes(src, config));

        if (src.getInfoAttivita() != null) {
            target.setDisabledStateInstitution(src.getInfoAttivita().getDisabledStateInstitution());
            target.setDescriptionStateInstitution(src.getInfoAttivita().getDescriptionStateInstitution());
        }

        return target;
    }

    private List<String> mapAtecoCodes(PdndVisuraImpresa src, PdndConfig config) {
        Set<String> atecoCodes = new HashSet<>();

        if (src.getInfoAttivita() != null &&
                src.getInfoAttivita().getClassificazioniAteco() != null) {

            atecoCodes.addAll(
                    src.getInfoAttivita().getClassificazioniAteco()
                            .getClassificazioniAteco()
                            .stream()
                            .map(ClassificazioneAteco::getCodiceAttivita)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet())
            );
        }

        if (Boolean.FALSE.equals(config.getSkipLocalizzazioneNodes()) &&
                src.getPointOfSales() != null &&
                src.getPointOfSales().getLocalizzazioni() != null) {

            atecoCodes.addAll(
                    src.getPointOfSales().getLocalizzazioni().stream()
                            .filter(loc -> loc.getClassificazioniAteco() != null)
                            .flatMap(loc -> loc.getClassificazioniAteco()
                                    .getClassificazioniAteco()
                                    .stream())
                            .map(ClassificazioneAteco::getCodiceAttivita)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet())
            );
        }

        return new ArrayList<>(atecoCodes);
    }

    private String mapAddress(Localizzazione loc) {
        if (loc == null) return null;

        return String.format("%s %s, %s",
                loc.getToponimo(),
                loc.getVia(),
                loc.getCivico());
    }
}
