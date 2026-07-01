package it.gov.pagopa.merchant.utils.validator;

import it.gov.pagopa.common.web.dto.ValidationErrorDetail;
import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.merchant.constants.PointOfSaleConstants;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.exception.custom.PosValidationException;
import it.gov.pagopa.merchant.utils.Utilities;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class PointOfSaleValidator {

    private final Validator validator;

    private static final String REGEX_PHONE = "^\\+?\\d{7,15}$";
    private static final String REGEX_LINK = "^https:\\/\\/(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}(?:\\/[^\\s]*)?$";
    private static final String REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    private final int posDefaultMaxSizeRequest;

    public PointOfSaleValidator(Validator validator,
                                @Value("${point-of-sales.defaultMaxSizeRequest}")int posDefaultMaxSizeRequest) {
        this.validator = validator;
        this.posDefaultMaxSizeRequest = posDefaultMaxSizeRequest;
    }

    public void validatePointOfSales(List<PointOfSaleDTO> pointOfSaleDTOS) {
        if (pointOfSaleDTOS == null || pointOfSaleDTOS.isEmpty()) {
            log.warn("[POS-VALIDATION] Validation failed: PointOfSale list is null or empty");
            throw new ClientExceptionWithBody(
                    HttpStatus.BAD_REQUEST,
                    PointOfSaleConstants.CODE_BAD_REQUEST,
                    PointOfSaleConstants.MSG_LIST_NOT_EMPTY);
        }
        if (pointOfSaleDTOS.size() > posDefaultMaxSizeRequest) {
            throw new ClientExceptionWithBody(
                    HttpStatus.BAD_REQUEST,
                    PointOfSaleConstants.CODE_BAD_REQUEST,
                    PointOfSaleConstants.MSG_POINT_OF_SALE_SIZE_EXCEEDED.formatted(posDefaultMaxSizeRequest));
        }
        log.debug("[POS-VALIDATION] Received {} PointOfSale entries to validate", pointOfSaleDTOS.size());

    }

    public void validateViolationsPointOfSales(List<PointOfSaleDTO> pointOfSaleDTOS) {
        List<ValidationErrorDetail> errors = new ArrayList<>();

        for (int i = 0; i < pointOfSaleDTOS.size(); i++) {
            PointOfSaleDTO dto = pointOfSaleDTOS.get(i);
            log.debug("[POS-VALIDATION] Validating PointOfSale entry at index {}", i);

            dto.setWebsite(StringUtils.lowerCase(StringUtils.trim(dto.getWebsite()), Locale.ROOT));
            dto.setChannelGeolink(StringUtils.lowerCase(StringUtils.trim(dto.getChannelGeolink()), Locale.ROOT));

            errors.addAll(validatePointOfSale(dto, i));
            errors.addAll(validateEmailAndWebsite(dto, i));
            errors.addAll(validateChannels(dto, i));
        }

        errors.addAll(validateDuplicates(pointOfSaleDTOS));

        if (!errors.isEmpty()) {
            Map<String, Map<String, Long>> grouped = errors.stream()
                    .collect(Collectors.groupingBy(
                            ValidationErrorDetail::getCode,
                            Collectors.groupingBy(ValidationErrorDetail::getMessage, Collectors.counting())
                    ));

            log.warn("[POINT-OF-SALES-VALIDATION] Validation failed: {} errors found across {} entries",
                    errors.size(), pointOfSaleDTOS.size());

            grouped.forEach((code, msgMap) -> msgMap.forEach((message, count) ->
                    log.warn("   - {} ({}): {} occurrence{}", code, message, count, count > 1 ? "s" : "")
            ));

            log.debug("[POS-VALIDATION] Validation error details: {}", errors);
            throw new PosValidationException(errors);
        }

        log.info("[POS-VALIDATION] Validation completed successfully: {} PointOfSale entries validated with no errors",
                pointOfSaleDTOS.size());
    }

    private List<ValidationErrorDetail> validateDuplicates(List<PointOfSaleDTO> pointOfSaleDTOS) {
        List<ValidationErrorDetail> errors = new ArrayList<>();
        Set<String> emails = new HashSet<>();
        Set<String> physicalPOS = new HashSet<>();
        Set<String> onlinePOS  = new HashSet<>();

        for (int i = 0; i < pointOfSaleDTOS.size(); i++) {
            PointOfSaleDTO pos = pointOfSaleDTOS.get(i);
            checkDuplicateEmail(pos, i, emails, errors);
            checkDuplicateByType(pos, i, physicalPOS, onlinePOS, errors);
        }

        return errors;
    }

    private void checkDuplicateEmail(PointOfSaleDTO pos, int index, Set<String> emails, List<ValidationErrorDetail> errors) {
        String email = pos.getContactEmail();
        if (StringUtils.isNotBlank(email) && !emails.add(email)) {
            errors.add(buildError(index, "contactEmail", email,
                    PointOfSaleConstants.CODE_ALREADY_REGISTERED,
                    "Duplicate email detected in the list"));
        }
    }

    private void checkDuplicateByType(PointOfSaleDTO pos, int index, Set<String> physicalPOS, Set<String> onlinePOS, List<ValidationErrorDetail> errors) {
        switch (pos.getType()) {
            case PHYSICAL -> checkDuplicatePhysical(pos, index, physicalPOS, errors);
            case ONLINE   -> checkDuplicateOnline(pos, index, onlinePOS, errors);
        }
    }

    private void checkDuplicatePhysical(PointOfSaleDTO pos, int index, Set<String> seen, List<ValidationErrorDetail> errors) {
        String key = pos.getType()
                + "|" + pos.getAddress()
                + "|" + pos.getStreetNumber()
                + "|" + pos.getCity()
                + "|" + Utilities.normalizeFranchiseName(pos.getFranchiseName());

        if (!seen.add(key)) {
            errors.add(buildError(index, "type|address|streetNumber|city|franchiseName", key,
                    PointOfSaleConstants.CODE_ALREADY_REGISTERED,
                    "Duplicate physical point of sale in request"));
        }
    }

    private void checkDuplicateOnline(PointOfSaleDTO pos, int index,  Set<String> seen,  List<ValidationErrorDetail> errors) {
                String key = pos.getType()
                + "|" + Utilities.sanitizeDomain(pos.getWebsite())
                + "|" + Utilities.normalizeFranchiseName(pos.getFranchiseName());

        if (!seen.add(key)) {
            errors.add(buildError(index, "type|website|franchiseName", key,
                    PointOfSaleConstants.CODE_ALREADY_REGISTERED,
                    "Duplicate online point of sale in request"));
        }
    }

    // --- core bean validation ---
    private List<ValidationErrorDetail> validatePointOfSale(PointOfSaleDTO pointOfSaleDTO, int index) {
        Set<ConstraintViolation<PointOfSaleDTO>> violations = switch (pointOfSaleDTO.getType().name().toUpperCase()) {
            case "PHYSICAL" -> validator.validate(pointOfSaleDTO, PhysicalGroup.class);
            case "ONLINE" -> validator.validate(pointOfSaleDTO, OnlineGroup.class);
            default -> Set.of();
        };

        return violations.stream()
                .map(v -> buildError(index, v.getPropertyPath().toString(), v.getInvalidValue(),
                        resolveCode(v), v.getMessage()))
                .toList();
    }

    private List<ValidationErrorDetail> validateEmailAndWebsite(PointOfSaleDTO dto, int index) {
        return Stream.of(
                validateChannelField(dto.getContactEmail(), "contactEmail", REGEX_EMAIL,
                        PointOfSaleConstants.CODE_INVALID_EMAIL, PointOfSaleConstants.MSG_INVALID_EMAIL, index),
                validateChannelField(dto.getWebsite(), "website", REGEX_LINK,
                        PointOfSaleConstants.CODE_INVALID_WEBSITE, PointOfSaleConstants.MSG_INVALID_WEBSITE, index)
        ).flatMap(List::stream).toList();
    }

    private List<ValidationErrorDetail> validateChannels(PointOfSaleDTO dto, int index) {
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

