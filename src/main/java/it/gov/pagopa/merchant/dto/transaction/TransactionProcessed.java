package it.gov.pagopa.merchant.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionProcessed {
    private String id;

    private String idTrxAcquirer;

    private String acquirerCode;

    private LocalDateTime trxDate;

    private String operationType;

    private String acquirerId;

    private String userId;

    private String correlationId;

    private Long amountCents;

    private Map<String, Reward> rewards;

    private Long effectiveAmountCents;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime trxChargeDate;
    private String operationTypeTranscoded;

    private LocalDateTime timestamp;
    private Map<String, String> additionalProperties;
    private InvoiceFile invoiceFile;
}

