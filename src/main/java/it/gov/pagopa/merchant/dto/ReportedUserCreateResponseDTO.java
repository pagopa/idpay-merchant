package it.gov.pagopa.merchant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportedUserCreateResponseDTO {
    private final String status;
    private final String errorKey;


    public ReportedUserCreateResponseDTO(String status, String errorKey) {
        this.status = status;
        this.errorKey = errorKey;
    }

    public static ReportedUserCreateResponseDTO ok() {
        return new ReportedUserCreateResponseDTO("OK", "");
    }
    public static ReportedUserCreateResponseDTO ko(String errorKey) {
        return new ReportedUserCreateResponseDTO("KO", errorKey);
    }

}

