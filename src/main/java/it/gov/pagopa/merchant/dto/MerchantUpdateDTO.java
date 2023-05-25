package it.gov.pagopa.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantUpdateDTO {
    private String status;

    private Integer errorRow;

    private String errorKey;

    private LocalDateTime elabTimeStamp;
}
