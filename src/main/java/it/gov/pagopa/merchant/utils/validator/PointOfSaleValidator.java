package it.gov.pagopa.merchant.utils.validator;

import it.gov.pagopa.common.web.dto.ValidationErrorDetail;
import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.common.web.exception.ValidationException;
import it.gov.pagopa.merchant.constants.MerchantConstants;
import it.gov.pagopa.merchant.dto.pointofsales.ChannelDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class PointOfSaleValidator{

    private final Validator validator;

    private static final String VALID_LINK = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    private static final String REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    public PointOfSaleValidator(Validator validator){
        this.validator = validator;
    }

    public void validatePointOfSales(List<PointOfSaleDTO> pointOfSaleDTOS){
        if(pointOfSaleDTOS == null || pointOfSaleDTOS.isEmpty()){
            throw new ClientExceptionWithBody(
                    HttpStatus.BAD_REQUEST,
                    MerchantConstants.ExceptionCode.POINT_OF_SALE_BAD_REQUEST,
                    "Point of sales list cannot be empty.");
        }
    }

    public void validateViolationsPointOfSales(List<PointOfSaleDTO> pointOfSaleDTOS) {
        List<ValidationErrorDetail> errors = new ArrayList<>();

        for (int i = 0; i < pointOfSaleDTOS.size(); i++) {
            PointOfSaleDTO dto = pointOfSaleDTOS.get(i);

            Set<ConstraintViolation<PointOfSaleDTO>> violations = Set.of();

            if("PHYSICAL".equalsIgnoreCase(dto.getType().name())){
                violations = validator.validate(dto, PhysicalGroup.class);
            }
            else if("ONLINE".equalsIgnoreCase(dto.getType().name())){
                violations = validator.validate(dto, OnlineGroup.class);
            }
            
            validateEmailAndWebsite(errors, i, dto);

            for(ConstraintViolation<?> constraintViolation : violations){
                errors.add(
                        buildError(i, constraintViolation.getPropertyPath().toString(), constraintViolation.getInvalidValue(), resolveCode(constraintViolation), constraintViolation.getMessage())
                );

            }

            if(dto.getChannels() != null){
                for(int j = 0; j < dto.getChannels().size(); j++){
                    ChannelDTO channelDTO = dto.getChannels().get(j);
                    Set<ConstraintViolation<ChannelDTO>> channelViolations = validator.validate(channelDTO, ValidationApiEnabledGroup.class);

                    for(ConstraintViolation<ChannelDTO> violation : channelViolations){
                        errors.add(
                                buildError(i, "channels["+j+"]."+violation.getPropertyPath(),violation.getInvalidValue(), violation.getMessage(), resolveCode(violation))
                        );
                    }
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
    
    private void validateEmailAndWebsite(List<ValidationErrorDetail> errors, int i, PointOfSaleDTO dto){
        if(StringUtils.isNotEmpty(dto.getContactEmail())){
            if(!dto.getWebsite().matches(REGEX_EMAIL)){
                errors.add(
                        buildError(i, "contactEmail", dto.getContactEmail(), "contactEmail must be a valid EMAIL", "INVALID_FORMAT")
                );
            }
        }
        if(StringUtils.isNotEmpty(dto.getWebsite())){
            if(!dto.getWebsite().matches(VALID_LINK)){
                errors.add(
                        buildError(i, "website", dto.getWebsite(), "website must be a valid https URL", "INVALID_URL")
                );
            }
        }
    }
    
    private String resolveCode(ConstraintViolation<?> violation){
        String annotation = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
        
        return switch (annotation){
            case "NotNull", "NotBlank", "NotEmpty" -> "FIELD_REQUIRED";
            case "Pattern" -> "INVALID_FORMAT";
            case "URL" -> "INVALID_URL";
            default -> "INVALID_VALUE";
        };
    }

    private ValidationErrorDetail buildError(int index, String field, Object value, String message, String code){
        return ValidationErrorDetail.builder()
                .index(index)
                .field(field)
                .value(value)
                .message(message)
                .code(code)
                .build();
    }
}