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

            validatePointOfSale(dto, i, errors);
            validateEmailAndWebsite(dto, i, errors);
            validateChannels(dto, i, errors);

        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private void validateChannels(PointOfSaleDTO pointOfSaleDTO, int pointOfSaleIndex, List<ValidationErrorDetail> errors){
        if(pointOfSaleDTO.getChannels() == null) return;
        for(int j = 0; j < pointOfSaleDTO.getChannels().size(); j++){
            ChannelDTO channelDTO = pointOfSaleDTO.getChannels().get(j);
            Set<ConstraintViolation<ChannelDTO>> channelViolations = validator.validate(channelDTO, ValidationApiEnabledGroup.class);

            for(ConstraintViolation<ChannelDTO> violation : channelViolations){
                String propertyPath = "channels["+j+"]."+violation.getPropertyPath();
                errors.add(
                        buildError(
                                pointOfSaleIndex,
                                propertyPath,
                                violation.getInvalidValue(),
                                violation.getMessage(),
                                resolveCode(violation))
                );
            }
        }
    }

    private void validatePointOfSale(PointOfSaleDTO pointOfSaleDTO, int index, List<ValidationErrorDetail> errors){
        Set<ConstraintViolation<PointOfSaleDTO>> violations = switch (pointOfSaleDTO.getType().name().toUpperCase()){
            case "PHYSICAL" -> validator.validate(pointOfSaleDTO, PhysicalGroup.class);
            case "ONLINE" -> validator.validate(pointOfSaleDTO, OnlineGroup.class);
            default -> Set.of();
        };

        for(ConstraintViolation<?> violation : violations){
            errors.add(buildError(
                    index,
                    violation.getPropertyPath().toString(),
                    violation.getInvalidValue(),
                    resolveCode(violation),
                    violation.getMessage()
            ));
        }
    }


    private void validateEmailAndWebsite(PointOfSaleDTO pointOfSaleDTO, int i, List<ValidationErrorDetail> errors){
        String email = pointOfSaleDTO.getContactEmail();
        String website = pointOfSaleDTO.getWebsite();

        if(StringUtils.isNotBlank(email) && !email.matches(REGEX_EMAIL)){
            errors.add(buildError(
                    i,
                    "contactEmail",
                    pointOfSaleDTO.getContactEmail(),
                    "contactEmail must be a valid EMAIL",
                    "INVALID_FORMAT")
            );
        }

        if(StringUtils.isNotBlank(website) && !website.matches(VALID_LINK)){
            errors.add(buildError(
                    i,
                    "website",
                    pointOfSaleDTO.getWebsite(),
                    "website must be a valid https URL",
                    "INVALID_URL")
            );
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