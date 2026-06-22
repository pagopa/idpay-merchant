package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.exception.custom.PosValidationException;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleFaker;
import it.gov.pagopa.merchant.utils.validator.PointOfSaleValidator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePointOfSaleServiceTest {

    @Mock
    private GetPointOfSaleService getPointOfSaleServiceMock;
    @Mock
    private PointOfSaleRepository pointOfSaleRepositoryMock;

    private UpdatePointOfSaleService service;

    private static final String POINT_OF_SALE_ID = "POS_ID";
    private static final String MERCHANT_ID = "MERCHANT_ID";

    @BeforeEach
    void setUp() {
        PointOfSaleValidator pointOfSaleValidator = new PointOfSaleValidator(
                Validation.buildDefaultValidatorFactory().getValidator(), 100);
        service = new UpdatePointOfSaleServiceImpl(
                getPointOfSaleServiceMock, pointOfSaleRepositoryMock, pointOfSaleValidator);
    }

    @Test
    void patchPointOfSale_updatesOperativeEmail() {
        PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
        PointOfSaleDTO patchDTO = PointOfSaleDTO.builder()
                .contactEmail("updated@email.it")
                .build();

        when(getPointOfSaleServiceMock.getPointOfSaleByIdAndMerchantId(POINT_OF_SALE_ID, MERCHANT_ID))
                .thenReturn(pointOfSale);
        when(pointOfSaleRepositoryMock.save(pointOfSale)).thenReturn(pointOfSale);

        PointOfSale result = service.patchPointOfSale(POINT_OF_SALE_ID, MERCHANT_ID, patchDTO);

        assertEquals("updated@email.it", result.getContactEmail());
        verify(pointOfSaleRepositoryMock).save(pointOfSale);
    }

    @Test
    void patchPointOfSale_updatesMultipleFields() {
        PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
        PointOfSaleDTO patchDTO = PointOfSaleDTO.builder()
                .contactEmail("updated@email.it")
                .channelPhone("39061234567")
                .city("Roma")
                .build();

        when(getPointOfSaleServiceMock.getPointOfSaleByIdAndMerchantId(POINT_OF_SALE_ID, MERCHANT_ID))
                .thenReturn(pointOfSale);
        when(pointOfSaleRepositoryMock.save(pointOfSale)).thenReturn(pointOfSale);

        PointOfSale result = service.patchPointOfSale(POINT_OF_SALE_ID, MERCHANT_ID, patchDTO);

        assertEquals("updated@email.it", result.getContactEmail());
        assertEquals("39061234567", result.getChannelPhone());
        assertEquals("Roma", result.getCity());
        verify(pointOfSaleRepositoryMock).save(pointOfSale);
    }

    @Test
    void patchPointOfSale_withInvalidOperativeEmail_throwsValidationException() {
        PointOfSaleDTO patchDTO = PointOfSaleDTO.builder()
                .contactEmail("invalid-email")
                .build();

        assertThrows(PosValidationException.class,
                () -> service.patchPointOfSale(POINT_OF_SALE_ID, MERCHANT_ID, patchDTO));
    }
}
