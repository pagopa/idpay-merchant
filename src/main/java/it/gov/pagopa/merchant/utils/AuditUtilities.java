package it.gov.pagopa.merchant.utils;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
@AllArgsConstructor
@Slf4j(topic = "AUDIT")
public class AuditUtilities {
    public static final String SRCIP;

    static {
        String srcIp;
        try {
            srcIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("Cannot determine the ip of the current host", e);
            srcIp="UNKNOWN";
        }
        SRCIP = srcIp;
    }
    private static final String CEF = String.format("CEF:0|PagoPa|IDPAY|1.0|7|User interaction|2| event=Merchant dstip=%s", SRCIP);
    private static final String CEF_PATTERN = CEF + " msg={} cs1Label=initiativeId cs1={} cs2Label=entityId cs2={} cs3Label=fileName cs3={}";
    private static final String CEF_BASE_PATTERN = CEF + " msg={} sdeletedMerchant={} cs1Label=initiativeId cs1={}";
    private static final String CEF_PATTERN_MERCHANT_FILE = CEF + " msg={} cs1Label=initiativeId cs1={}";

    private void logAuditString(String pattern, String... parameters) {
        log.info(pattern, (Object[]) parameters);
    }

    public void logUploadMerchantOK(String initiativeId, String entityId, String fileName) {
        logAuditString(
                CEF_PATTERN,
                "Upload Merchants file completed.", initiativeId, entityId, fileName
        );
    }

    public void logValidationMerchantOK(String initiativeId, String organizationId, String fileName) {
        logAuditString(
                CEF_PATTERN,
                "Validation of Merchants file completed.", initiativeId, organizationId, fileName
        );
    }

    public void logUploadMerchantKO(String initiativeId, String organizationId, String fileName, String msg) {
        logAuditString(
                CEF_PATTERN,
                "Upload Merchants file failed: " + msg, initiativeId, organizationId, fileName
        );
    }

    public void logSavingMerchantsOK(String initiativeId, String organizationId, String fileName) {
        logAuditString(
                CEF_PATTERN,
                "Saving Merchants completed.", initiativeId, organizationId, fileName
        );
    }
    public void logDeleteMerchant(Long deletedMerchant, String initiativeId) {
        logAuditString(
                CEF_BASE_PATTERN,
                "Merchants deleted",deletedMerchant.toString(), initiativeId);
    }
    public void logDeleteMerchantFile(String initiativeId) {
        logAuditString(
                CEF_PATTERN_MERCHANT_FILE,
                "Merchant files deleted", initiativeId);
    }

}