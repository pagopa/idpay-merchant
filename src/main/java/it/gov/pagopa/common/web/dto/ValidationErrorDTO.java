package it.gov.pagopa.common.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidationErrorDTO extends ErrorDTO {

    private int status;

    private String error;
    private String message;

    private List<ValidationErrorDetail> details;
}
