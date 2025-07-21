package it.gov.pagopa.merchant.utils.validator;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.common.web.exception.ValidationException;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.pointofsales.ChannelDTO;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleDTOFaker;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {PointOfSaleValidator.class})
@ExtendWith(SpringExtension.class)
class PointOfSaleValidatorTest {

    @Autowired
    private PointOfSaleValidator pointOfSaleValidator;

    @MockBean
    private Validator validator;


    @Test
    void validatePointOfSales() {
        ClientExceptionWithBody exception = assertThrows(ClientExceptionWithBody.class,
                () -> pointOfSaleValidator.validatePointOfSales(null));

        assertEquals("Point of sales list cannot be empty.", exception.getMessage());
    }

    @Test
    void validatePointOfSales2() {
        ClientExceptionWithBody exception = assertThrows(ClientExceptionWithBody.class,
                () -> pointOfSaleValidator.validatePointOfSales(new ArrayList<>()));

        assertEquals("Point of sales list cannot be empty.", exception.getMessage());
    }

    @Test
    void validateViolationsPointOfSalesWhenPhysicalTypeShouldUsePhysicalGroup() {
        PointOfSaleDTO dto = PointOfSaleDTOFaker.mockInstance();
        dto.setType(PointOfSaleTypeEnum.PHYSICAL);

        List<PointOfSaleDTO> list = List.of(dto);

        when(validator.validate(dto, PhysicalGroup.class)).thenReturn(Collections.emptySet());

        assertDoesNotThrow(() -> pointOfSaleValidator.validateViolationsPointOfSales(list));

        verify(validator).validate(dto, PhysicalGroup.class);
    }

    @Test
    void validateViolationsPointOfSalesWhenOnlineTypeShouldUseOnlineGroup() {
        PointOfSaleDTO dto = PointOfSaleDTOFaker.mockInstance();
        dto.setType(PointOfSaleTypeEnum.ONLINE);

        List<PointOfSaleDTO> list = List.of(dto);

        when(validator.validate(dto, OnlineGroup.class)).thenReturn(Collections.emptySet());

        assertDoesNotThrow(() -> pointOfSaleValidator.validateViolationsPointOfSales(list));

        verify(validator).validate(dto, OnlineGroup.class);
    }


    @Test
    void validateChannelsWithViolationsBuildErrorCalled() {
        ChannelDTO channelDTO = new ChannelDTO();
        PointOfSaleDTO dto = PointOfSaleDTOFaker.mockInstance();
        dto.setChannels(List.of(channelDTO));
        List<PointOfSaleDTO> list = List.of(dto);

        ConstraintViolation<ChannelDTO> violation = mock(ConstraintViolation.class);
        Path mockPath = mock(Path.class);
        when(mockPath.toString()).thenReturn("someField");
        when(violation.getPropertyPath()).thenReturn(mockPath);
        when(violation.getInvalidValue()).thenReturn("badValue");
        when(violation.getMessage()).thenReturn("must not be null");

        ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
        NotNull mockAnnotation = mock(NotNull.class);
        when(mockAnnotation.annotationType()).thenReturn((Class) NotNull.class);
        when(descriptor.getAnnotation()).thenReturn(mockAnnotation);
        when(violation.getConstraintDescriptor()).thenReturn((ConstraintDescriptor) descriptor);

        Set<ConstraintViolation<ChannelDTO>> violations = Set.of(violation);

        when(validator.validate(dto)).thenReturn(Collections.emptySet());
        when(validator.validate(channelDTO, ValidationApiEnabledGroup.class)).thenReturn(violations);

        PointOfSaleValidator spyValidator = Mockito.spy(pointOfSaleValidator);

        Assertions.assertThrows(ValidationException.class, () -> {
            spyValidator.validateViolationsPointOfSales(list);
        });

    }

    @Test
    void validateEmailAndWebsiteInvalidEmailShouldAddError() {
        PointOfSaleDTO dto = PointOfSaleDTOFaker.mockInstance();
        dto.setContactEmail("invalidEmail");
        dto.setWebsite("notAnEmail.com");

        List<PointOfSaleDTO> list = List.of(dto);

        when(validator.validate(dto)).thenReturn(Collections.emptySet());

        ValidationException exception = Assertions.assertThrows(ValidationException.class, () -> {
            pointOfSaleValidator.validateViolationsPointOfSales(list);
        });

        Assertions.assertFalse(exception.getMessage().contains("contactEmail must be a valid EMAIL"));
    }

}