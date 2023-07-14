package it.gov.pagopa.merchant.utils;

import ch.qos.logback.classic.LoggerContext;
import it.gov.pagopa.common.utils.MemoryAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditUtilitiesTest {

    private static final String CEF = String.format("CEF:0|PagoPa|IDPAY|1.0|7|User interaction|2| event=Merchant dstip=%s", AuditUtilities.SRCIP);
    private static final String INITIATIVE_ID = "TEST_INITIATIVE_ID";
    private static final String ORGANIZATION_ID = "TEST_ORG_ID";
    private static final String FILE_NAME = "TEST_FILE_NAME";
    private final AuditUtilities auditUtilities = new AuditUtilities();
    private MemoryAppender memoryAppender;


    @BeforeEach
    public void setup() {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("AUDIT");
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(ch.qos.logback.classic.Level.INFO);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @Test
    void logUploadMerchantOK() {
        auditUtilities.logUploadMerchantOK(INITIATIVE_ID, ORGANIZATION_ID, FILE_NAME);

        assertEquals(
                CEF + " msg=Upload Merchants file completed."
                        + " cs1Label=initiativeId cs1=%s cs2Label=entityId cs2=%s cs3Label=fileName cs3=%s"
                                .formatted(INITIATIVE_ID, ORGANIZATION_ID, FILE_NAME),
                memoryAppender.getLoggedEvents().get(0).getFormattedMessage()
        );
    }

    @Test
    void logValidationMerchantOK() {
        auditUtilities.logValidationMerchantOK(INITIATIVE_ID, ORGANIZATION_ID, FILE_NAME);

        assertEquals(
                CEF + " msg=Validation of Merchants file completed."
                        + " cs1Label=initiativeId cs1=%s cs2Label=entityId cs2=%s cs3Label=fileName cs3=%s"
                        .formatted(INITIATIVE_ID, ORGANIZATION_ID, FILE_NAME),
                memoryAppender.getLoggedEvents().get(0).getFormattedMessage()
        );
    }

    @Test
    void logUploadMerchantKO() {
        auditUtilities.logUploadMerchantKO(INITIATIVE_ID, ORGANIZATION_ID, FILE_NAME, "error");

        assertEquals(
                CEF + " msg=Upload Merchants file failed: error"
                        + " cs1Label=initiativeId cs1=%s cs2Label=entityId cs2=%s cs3Label=fileName cs3=%s"
                        .formatted(INITIATIVE_ID, ORGANIZATION_ID, FILE_NAME),
                memoryAppender.getLoggedEvents().get(0).getFormattedMessage()
        );
    }

    @Test
    void logSavingMerchantsOK() {
        auditUtilities.logSavingMerchantsOK(INITIATIVE_ID, ORGANIZATION_ID, FILE_NAME);

        assertEquals(
                CEF + " msg=Saving Merchants completed."
                        + " cs1Label=initiativeId cs1=%s cs2Label=entityId cs2=%s cs3Label=fileName cs3=%s"
                        .formatted(INITIATIVE_ID, ORGANIZATION_ID, FILE_NAME),
                memoryAppender.getLoggedEvents().get(0).getFormattedMessage()
        );
    }
}
