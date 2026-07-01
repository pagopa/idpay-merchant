package it.gov.pagopa.merchant.dto.pointofsales;

import java.time.Instant;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PointOfSaleInitiativeDTO {

    private String initiativeId;
    private String initiativeName;
    private String organizationName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
