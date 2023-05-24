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
@Document(collection = "merchant_file")
public class MerchantFile {
    @MongoId
    private String fileName;
    @MongoId
    private String initiativeId;
    private String organizationId;
    private String organizationUserId;
    private String status;
    private LocalDateTime uploadDate;
    private boolean enabled;
}
