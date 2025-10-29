package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.connector.encrypt.EncryptRestConnector;
import it.gov.pagopa.merchant.dto.CFDTO;
import it.gov.pagopa.merchant.exception.custom.PDVInvocationException;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class PDVServiceImpl implements PDVService {

    private final EncryptRestConnector encryptRestConnector;

    public PDVServiceImpl(EncryptRestConnector encryptRestConnector) {
        this.encryptRestConnector = encryptRestConnector;
    }

    @Override
    public String encryptCF(String fiscalCode) {
        return wrapPDVCall(() -> encryptRestConnector.upsertToken(new CFDTO(fiscalCode)).getToken(),
                "An error occurred during encryption");
    }


    private String wrapPDVCall(Supplier<String> action, String errorMessage) {
        try {
            return action.get();
        } catch (Exception e) {
            throw new PDVInvocationException(errorMessage, true, e);
        }
    }
}
