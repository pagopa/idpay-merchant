package it.gov.pagopa.merchant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Document(collection = "point_of_sales_initiative")
public class PointOfSalesInitiative {

    @MongoId
    private String id;

    private String pointOfSaleId;
    private String initiativeId;
    private String merchantId;
    private Boolean enabled;
    private Instant createdAt;
    private Instant onboardingDate;
    private Instant updatedAt;
}
