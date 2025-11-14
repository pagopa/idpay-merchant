package it.gov.pagopa.merchant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "reported_user")
public class ReportedUser {
    @MongoId
    private String reportedUserId;
    private String merchantId;
    private String initiativeId;
    private String userId;
    private String transactionId;
    private LocalDateTime trxChargeDate;
    private LocalDateTime createdAt;
}
