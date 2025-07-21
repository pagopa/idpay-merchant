package it.gov.pagopa.common.web.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class ValidationErrorDTO extends ErrorDTO {

    private List<ValidationErrorDetail> details;

}
