package it.gov.pagopa.merchant.utils.validator;

import it.gov.pagopa.common.web.dto.ValidationErrorDetail;
import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.common.web.exception.ValidationException;
import it.gov.pagopa.merchant.constants.PointOfSaleConstants;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

import static it.gov.pagopa.merchant.utils.Utilities.sanitizeString;

@Component
@Slf4j
public class PointOfSaleValidator {

    private final Validator validator;

    private static final String REGEX_PHONE = "^\\+?\\d{7,15}$";
    private static final String REGEX_LINK = "^(http|https|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    private static final String REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    public PointOfSaleValidator(Validator validator) {
        this.validator = validator;
    }

    public void validatePointOfSales(List<PointOfSaleDTO> pointOfSaleDTOS) {
        log.info("[POINT-OF-SALES-VALIDATION] Checking if PointOfSale list is not empty");
        if (pointOfSaleDTOS == null || pointOfSaleDTOS.isEmpty()) {
            log.warn("[POINT-OF-SALES-VALIDATION]  Validation failed: PointOfSale list is null or empty");
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
            log.info("[POINT-OF-SALES-VALIDATION]  Validating PointOfSale at index {}: {}", i, sanitizeString(dto.toString()));

            errors.addAll(validatePointOfSale(dto, i));
            errors.addAll(validateEmailAndWebsite(dto, i));
            errors.addAll(validateChannels(dto, i));
        }

        errors.addAll(validateDuplicates(pointOfSaleDTOS));

        if (!errors.isEmpty()) {
            log.warn("[POINT-OF-SALES-VALIDATION]  Validation failed: {} errors found", errors.size());
            log.debug("[POINT-OF-SALES-VALIDATION]  Validation errors: {}", errors);
            throw new ValidationException(errors);
        }
        log.info("[POINT-OF-SALES-VALIDATION]  Validation completed successfully: no errors found");
    }

    private List<ValidationErrorDetail> validateDuplicates(List<PointOfSaleDTO> pointOfSaleDTOS) {
        log.info("[POINT-OF-SALES-VALIDATION]  Checking for duplicate emails in PointOfSale list");
        List<ValidationErrorDetail> errors = new ArrayList<>();
        Set<String> emails = new HashSet<>();

        for (int i = 0; i < pointOfSaleDTOS.size(); i++) {
            String email = pointOfSaleDTOS.get(i).getContactEmail();
            if (StringUtils.isNotBlank(email) && !emails.add(email)) {
                log.warn("[POINT-OF-SALES-VALIDATION]  Duplicate email detected at index {}: {}", i, sanitizeString(email));
                errors.add(buildError(i, "contactEmail", email,
                        PointOfSaleConstants.CODE_ALREADY_REGISTERED,
                        "Duplicate email in the provided list"));
            }
        }
        return errors;
    }

    private List<ValidationErrorDetail> validatePointOfSale(PointOfSaleDTO pointOfSaleDTO, int index) {
        log.info("[POINT-OF-SALES-VALIDATION]  Validating type-specific constraints for PointOfSale at index {}", index);
        Set<ConstraintViolation<PointOfSaleDTO>> violations = switch (pointOfSaleDTO.getType().name().toUpperCase()) {
            case "PHYSICAL" -> validator.validate(pointOfSaleDTO, PhysicalGroup.class);
            case "ONLINE" -> validator.validate(pointOfSaleDTO, OnlineGroup.class);
            default -> Set.of();
        };

        return violations.stream()
                .peek(v -> log.warn("[POINT-OF-SALES-VALIDATION]  Constraint violation at index {}: {} - {}",
                        index, v.getPropertyPath(), sanitizeString(v.getMessage())))
                .map(v -> buildError(index, v.getPropertyPath().toString(), v.getInvalidValue(),
                        resolveCode(v), v.getMessage()))
                .toList();
    }

    private List<ValidationErrorDetail> validateEmailAndWebsite(PointOfSaleDTO dto, int index) {
        log.info("[POINT-OF-SALES-VALIDATION]  Validating email and website for PointOfSale at index {}", index);
        return Stream.of(
                validateChannelField(dto.getContactEmail(), "contactEmail", REGEX_EMAIL,
                        PointOfSaleConstants.CODE_INVALID_EMAIL, PointOfSaleConstants.MSG_INVALID_EMAIL, index),
                validateChannelField(dto.getWebsite(), "website", REGEX_LINK,
                        PointOfSaleConstants.CODE_INVALID_WEBSITE, PointOfSaleConstants.MSG_INVALID_WEBSITE, index)
        ).flatMap(List::stream).toList();
    }

    private List<ValidationErrorDetail> validateChannels(PointOfSaleDTO dto, int index) {
        log.info("[POINT-OF-SALES-VALIDATION]  Validating channels for PointOfSale at index {}", index);
        return Stream.of(
                validateChannelField(dto.getChannelEmail(), "channelEmail", REGEX_EMAIL,
                        PointOfSaleConstants.CODE_INVALID_EMAIL, PointOfSaleConstants.MSG_INVALID_EMAIL, index),
                validateChannelField(dto.getChannelGeolink(), "channelGeolink", REGEX_LINK,
                        PointOfSaleConstants.CODE_INVALID_WEBSITE, PointOfSaleConstants.MSG_INVALID_WEBSITE, index),
                validateChannelField(dto.getChannelPhone(), "channelPhone", REGEX_PHONE,
                        PointOfSaleConstants.CODE_INVALID_MOBILE, PointOfSaleConstants.MSG_INVALID_MOBILE, index)
        ).flatMap(List::stream).toList();
    }

    private List<ValidationErrorDetail> validateChannelField(String value, String field, String regex,
                                                             String errorCode, String message, int index) {
        if (isInvalidFormat(value, regex)) {
            log.info("[POINT-OF-SALES-VALIDATION]  Invalid format detected for field '{}' at index {}: {}",
                    field, index, sanitizeString(value));
            return List.of(buildError(index, field, value, errorCode, message));
        }
        return Collections.emptyList();
    }

    private boolean isInvalidFormat(String value, String regex) {
        return StringUtils.isNotBlank(value) && !value.matches(regex);
    }

    private String resolveCode(ConstraintViolation<?> violation) {
        String annotation = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
        return switch (annotation) {
            case "NotNull", "NotBlank", "NotEmpty" -> PointOfSaleConstants.CODE_FIELD_REQUIRED;
            case "Pattern" -> PointOfSaleConstants.CODE_INVALID_FORMAT;
            case "URL" -> PointOfSaleConstants.CODE_INVALID_URL;
            default -> PointOfSaleConstants.CODE_INVALID_VALUE;
        };
    }

    private ValidationErrorDetail buildError(int index, String field, Object value, String code, String message) {
        return ValidationErrorDetail.builder()
                .index(index)
                .field(field)
                .value(value)
                .message(message)
                .code(code)
                .build();
    }

}
