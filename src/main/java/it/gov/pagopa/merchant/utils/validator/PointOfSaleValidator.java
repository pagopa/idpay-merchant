package it.gov.pagopa.merchant.utils.validator;

import it.gov.pagopa.common.web.dto.ValidationErrorDetail;
import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.common.web.exception.ValidationException;
import it.gov.pagopa.merchant.constants.PointOfSaleConstants;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PointOfSaleValidator{

    private final Validator validator;

    private static final String REGEX_PHONE = "^\\+?\\d{7,15}$";
    private static final String REGEX_LINK = "^(https|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    private static final String REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    public PointOfSaleValidator(Validator validator){
        this.validator = validator;
    }

    public void validatePointOfSales(List<PointOfSaleDTO> pointOfSaleDTOS){
        if(pointOfSaleDTOS == null || pointOfSaleDTOS.isEmpty()){
            throw new ClientExceptionWithBody(
                    HttpStatus.BAD_REQUEST,
                    PointOfSaleConstants.CODE_BAD_REQUEST,
                    PointOfSaleConstants.MSG_LIST_NOT_EMPTY);
        }
    }

    public void validateViolationsPointOfSales(List<PointOfSaleDTO> pointOfSaleDTOS) {
        List<ValidationErrorDetail> errors = new ArrayList<>();

        for (int i = 0; i < pointOfSaleDTOS.size(); i++) {
            PointOfSaleDTO dto = pointOfSaleDTOS.get(i);

            errors.addAll(validatePointOfSale(dto, i));
            errors.addAll(validateEmailAndWebsite(dto, i));
            errors.addAll(validateChannels(dto, i));

        }

        errors.addAll(validateDuplicates(pointOfSaleDTOS));

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private List<ValidationErrorDetail> validateDuplicates(List<PointOfSaleDTO> pointOfSaleDTOS){
        List<ValidationErrorDetail> errors = new ArrayList<>();
        Set<String> emails = new HashSet<>();
        
        for(int i = 0; i < pointOfSaleDTOS.size(); i++){
            String email = pointOfSaleDTOS.get(i).getContactEmail();
            if(StringUtils.isNotBlank(email) && !emails.add(email)){
                    errors.add(buildError(
                            i,
                            "contactEmail",
                            email,
                            PointOfSaleConstants.CODE_ALREADY_REGISTERED,
                            "Duplicate email in the provided list"
                    ));
            }
        }
        return errors;
    }

    private List<ValidationErrorDetail> validatePointOfSale(PointOfSaleDTO pointOfSaleDTO, int index){
        List<ValidationErrorDetail> errors = new ArrayList<>();
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
        
        return  errors;
    }


    private List<ValidationErrorDetail> validateEmailAndWebsite(PointOfSaleDTO pointOfSaleDTO, int index){
        List<ValidationErrorDetail> errors = new ArrayList<>();

        errors.addAll(validateChannelField(pointOfSaleDTO.getContactEmail(), "contactEmail", REGEX_EMAIL,
                PointOfSaleConstants.CODE_INVALID_EMAIL, PointOfSaleConstants.MSG_INVALID_EMAIL,
                index));

        errors.addAll(validateChannelField(pointOfSaleDTO.getWebsite(), "website", REGEX_LINK,
                PointOfSaleConstants.CODE_INVALID_WEBSITE, PointOfSaleConstants.MSG_INVALID_WEBSITE,
                index));

        return errors;
    }

    private List<ValidationErrorDetail> validateChannels(PointOfSaleDTO pointOfSaleDTO, int index){
        List<ValidationErrorDetail> errors = new ArrayList<>();

        errors.addAll(validateChannelField(pointOfSaleDTO.getChannelEmail(), "channelEmail", REGEX_EMAIL,
                PointOfSaleConstants.CODE_INVALID_EMAIL, PointOfSaleConstants.MSG_INVALID_EMAIL,
                index));

        errors.addAll(validateChannelField(pointOfSaleDTO.getChannelWebsite(), "channelWebsite", REGEX_LINK,
                PointOfSaleConstants.CODE_INVALID_WEBSITE, PointOfSaleConstants.MSG_INVALID_WEBSITE,
                index));

        errors.addAll(validateChannelField(pointOfSaleDTO.getChannelGeolink(), "channelGeolink", REGEX_LINK,
                PointOfSaleConstants.CODE_INVALID_WEBSITE, PointOfSaleConstants.MSG_INVALID_WEBSITE,
                index));

        errors.addAll(validateChannelField(pointOfSaleDTO.getChannelPhone(), "channelPhone", REGEX_PHONE,
                PointOfSaleConstants.CODE_INVALID_MOBILE, PointOfSaleConstants.MSG_INVALID_MOBILE,
                index));

        return errors;
    }
    
    private List<ValidationErrorDetail> validateChannelField(String value, String field, String regex, String errorCode, String message, int index){
        List<ValidationErrorDetail> errors = new ArrayList<>();
        if(isInvalidFormat(value, regex)){
            errors.add(buildError(index, field, value, errorCode, message));
        }
        return errors;
    }
    
    private boolean isInvalidFormat(String value, String regex){
        return StringUtils.isNotBlank(value) && !value.matches(regex);
    }

    private String resolveCode(ConstraintViolation<?> violation){
        String annotation = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();

        return switch (annotation){
            case "NotNull", "NotBlank", "NotEmpty" -> PointOfSaleConstants.CODE_FIELD_REQUIRED;
            case "Pattern" -> PointOfSaleConstants.CODE_INVALID_FORMAT;
            case "URL" -> PointOfSaleConstants.CODE_INVALID_URL;
            default -> PointOfSaleConstants.CODE_INVALID_VALUE;
        };
    }

    private ValidationErrorDetail buildError(int index, String field, Object value, String code, String message){
        return ValidationErrorDetail.builder()
                .index(index)
                .field(field)
                .value(value)
                .message(message)
                .code(code)
                .build();
    }
}