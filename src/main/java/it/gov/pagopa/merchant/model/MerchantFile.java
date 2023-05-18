package it.gov.pagopa.merchant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "merchant_file")
public class MerchantFile {
    private String fileName;
    private String initiativeId;
    private String organizationId;
    private String status;
    private LocalDateTime uploadDate;
    private boolean enabled;
}
