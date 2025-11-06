package it.gov.pagopa.merchant.service;


import it.gov.pagopa.merchant.connector.decrypt.DecryptRestConnector;
import it.gov.pagopa.merchant.exception.custom.PDVInvocationException;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class PDVServiceImpl implements PDVService {

    private final DecryptRestConnector decryptRestConnector;

    public PDVServiceImpl(DecryptRestConnector decryptRestConnector) {
        this.decryptRestConnector = decryptRestConnector;
    }

    @Override
    public String decryptCF(String userId) {
        return wrapPDVCall(() -> decryptRestConnector.getPiiByToken(userId).getPii()
        );
    }

    private String wrapPDVCall(Supplier<String> action) {
        try {
            return action.get();
        } catch (Exception e) {
            throw new PDVInvocationException("An error occurred during decryption", true, e);
        }
    }
}
