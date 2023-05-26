package it.gov.pagopa.merchant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "merchant_file")
@CompoundIndex(name = "merchant_file_unique_idx", def = "{'fileName': 1, 'initiativeId': 1}", unique = true)
public class MerchantFile {
    private String fileName;
    private String initiativeId;
    private String organizationId;
    private String organizationUserId;
    private String status;
    private LocalDateTime uploadDate;
    private boolean enabled;
}
