package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleDuplicateException;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.model.PointOfSalesInitiative;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.repository.PointOfSalesInitiativeRepository;
import it.gov.pagopa.merchant.service.KeycloakService;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleDTOFaker;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleFaker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavePointOfSaleServiceTest {

    @Mock private MerchantService merchantServiceMock;
    @Mock private PointOfSaleRepository pointOfSaleRepositoryMock;
    @Mock private KeycloakService keycloakServiceMock;
    @Mock private PointOfSaleDTOMapper pointOfSaleDTOMapperMock;
    @Mock private PointOfSalesInitiativeRepository pointOfSalesInitiativeRepositoryMock;
    @Mock private UsersResource usersResourceMock;
    @Mock private UserResource userResourceMock;

    private SavePointOfSaleServiceImpl service;

    private static final String MERCHANT_ID = "MERCHANT-ID";
    private static final String INITIATIVE_ID = "INITIATIVE";

    @BeforeEach
    void setUp() {
        service = new SavePointOfSaleServiceImpl(
                merchantServiceMock,
                pointOfSaleRepositoryMock,
                keycloakServiceMock,
                pointOfSaleDTOMapperMock,
                pointOfSalesInitiativeRepositoryMock
        );
    }

    private PointOfSaleDTO dto() {
        return PointOfSaleDTOFaker.mockInstance();
    }

    private PointOfSale entity(String id) {
        PointOfSale pos = PointOfSaleFaker.mockInstance();
        pos.setId(id);
        return pos;
    }

    @AfterEach
    void mockitoVerify() {
        Mockito.verifyNoMoreInteractions(
                merchantServiceMock,
                pointOfSaleRepositoryMock,
                keycloakServiceMock,
                pointOfSaleDTOMapperMock,
                pointOfSalesInitiativeRepositoryMock);
    }

    @Test
    void shouldSaveNewPos_andCreateAssociation() {
        PointOfSaleDTO dto = dto();
        PointOfSale entity = entity("POS-1");

        doNothing().when(merchantServiceMock).verifyMerchantExists(MERCHANT_ID);
        when(pointOfSaleDTOMapperMock.dtoToEntity(dto, MERCHANT_ID)).thenReturn(entity);
        when(pointOfSaleRepositoryMock.findDuplicate(MERCHANT_ID, entity)).thenReturn(Optional.empty());
        when(pointOfSaleRepositoryMock.save(entity)).thenReturn(entity);

        service.savePointOfSales(MERCHANT_ID, INITIATIVE_ID, List.of(dto));

        verify(pointOfSaleRepositoryMock).save(entity);
        verify(keycloakServiceMock).manageReferentUserOnKeycloak(eq(entity), eq(dto.getContactEmail()));
        verify(pointOfSalesInitiativeRepositoryMock).saveAll(anySet());
    }

    @Test
    void shouldNotSavePos_whenDuplicateExists_andCreateAssociation() {
        PointOfSaleDTO dto = dto();
        PointOfSale duplicate = entity("POS-DUP");

        doNothing().when(merchantServiceMock).verifyMerchantExists(MERCHANT_ID);
        when(pointOfSaleDTOMapperMock.dtoToEntity(dto, MERCHANT_ID)).thenReturn(duplicate);
        when(pointOfSaleRepositoryMock.findDuplicate(MERCHANT_ID, duplicate)).thenReturn(Optional.of(duplicate));
        when(pointOfSalesInitiativeRepositoryMock
                .findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue("POS-DUP", INITIATIVE_ID, MERCHANT_ID))
                .thenReturn(Optional.empty());

        service.savePointOfSales(MERCHANT_ID, INITIATIVE_ID, List.of(dto));

        verify(pointOfSaleRepositoryMock, never()).save(any());
        verify(pointOfSalesInitiativeRepositoryMock).saveAll(anySet());
    }

    @Test
    void shouldThrowDuplicateException_whenAllAssociationsAlreadyExist() {
        PointOfSaleDTO dto = dto();
        PointOfSale duplicate = entity("POS-1");

        doNothing().when(merchantServiceMock).verifyMerchantExists(MERCHANT_ID);
        when(pointOfSaleDTOMapperMock.dtoToEntity(dto, MERCHANT_ID)).thenReturn(duplicate);
        when(pointOfSaleRepositoryMock.findDuplicate(MERCHANT_ID, duplicate)).thenReturn(Optional.of(duplicate));
        when(pointOfSalesInitiativeRepositoryMock
                .findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue("POS-1", INITIATIVE_ID, MERCHANT_ID))
                .thenReturn(Optional.of(new PointOfSalesInitiative()));

        assertThrows(PointOfSaleDuplicateException.class,
                () -> service.savePointOfSales(MERCHANT_ID, INITIATIVE_ID, List.of(dto)));

        verify(pointOfSaleRepositoryMock, never()).save(any());
        verify(pointOfSalesInitiativeRepositoryMock, never()).saveAll(anySet());
    }

    @Test
    void shouldThrowDuplicateException_onDuplicateKeyFromRepository() {
        PointOfSaleDTO dto = dto();
        PointOfSale entity = PointOfSaleFaker.mockInstance();

        doNothing().when(merchantServiceMock).verifyMerchantExists(MERCHANT_ID);
        when(pointOfSaleDTOMapperMock.dtoToEntity(dto, MERCHANT_ID)).thenReturn(entity);
        when(pointOfSaleRepositoryMock.findDuplicate(MERCHANT_ID, entity)).thenReturn(Optional.empty());
        when(pointOfSaleRepositoryMock.save(entity)).thenThrow(new DuplicateKeyException("dup"));

        assertThrows(PointOfSaleDuplicateException.class,
                () -> service.savePointOfSales(MERCHANT_ID, INITIATIVE_ID, List.of(dto)));

        verify(pointOfSalesInitiativeRepositoryMock)
                .deleteByMerchantIdAndInitiativeIdAndPointOfSaleIdIn(MERCHANT_ID, INITIATIVE_ID, List.of());
    }

    @Test
    void shouldThrowServiceException_onIncorrectResultSizeDataAccessException() {
        PointOfSaleDTO dto = dto();
        PointOfSale entity = PointOfSaleFaker.mockInstance();

        doNothing().when(merchantServiceMock).verifyMerchantExists(MERCHANT_ID);
        when(pointOfSaleDTOMapperMock.dtoToEntity(dto, MERCHANT_ID)).thenReturn(entity);
        when(pointOfSaleRepositoryMock.findDuplicate(MERCHANT_ID, entity))
                .thenThrow(new IncorrectResultSizeDataAccessException(1));

        assertThrows(ServiceException.class,
                () -> service.savePointOfSales(MERCHANT_ID, INITIATIVE_ID, List.of(dto)));

        verify(pointOfSalesInitiativeRepositoryMock)
                .deleteByMerchantIdAndInitiativeIdAndPointOfSaleIdIn(MERCHANT_ID, INITIATIVE_ID, List.of());
    }

    @Test
    void shouldTriggerCompensation_andThrowServiceException_onGenericError() {
        PointOfSaleDTO dto = dto();
        PointOfSale entity = entity("POS-1");

        doNothing().when(merchantServiceMock).verifyMerchantExists(MERCHANT_ID);
        when(pointOfSaleDTOMapperMock.dtoToEntity(dto, MERCHANT_ID)).thenReturn(entity);
        when(pointOfSaleRepositoryMock.findDuplicate(MERCHANT_ID, entity)).thenReturn(Optional.empty());
        when(pointOfSaleRepositoryMock.save(entity)).thenReturn(entity);
        doThrow(new RuntimeException("DB error"))
                .when(pointOfSalesInitiativeRepositoryMock).saveAll(anySet());

        when(keycloakServiceMock.getUserResource()).thenReturn(usersResourceMock);
        when(usersResourceMock.searchByEmail(entity.getContactEmail(), true)).thenReturn(List.of());

        assertThrows(ServiceException.class,
                () -> service.savePointOfSales(MERCHANT_ID, INITIATIVE_ID, List.of(dto)));

        verify(keycloakServiceMock).manageReferentUserOnKeycloak(eq(entity), eq(dto.getContactEmail()));
        verify(pointOfSalesInitiativeRepositoryMock)
                .deleteByMerchantIdAndInitiativeIdAndPointOfSaleIdIn(MERCHANT_ID, INITIATIVE_ID, List.of("POS-1"));
        verify(pointOfSaleRepositoryMock).deleteById("POS-1");
        verify(keycloakServiceMock).getUserResource();
        verify(usersResourceMock).searchByEmail(entity.getContactEmail(), true);
    }

    @Test
    void shouldTriggerCompensation_andDeleteKeycloakUser_whenUserExists() {
        PointOfSaleDTO dto = dto();
        PointOfSale entity = entity("POS-1");

        UserRepresentation userRep = new UserRepresentation();
        userRep.setId("KC-USER-1");

        doNothing().when(merchantServiceMock).verifyMerchantExists(MERCHANT_ID);
        when(pointOfSaleDTOMapperMock.dtoToEntity(dto, MERCHANT_ID)).thenReturn(entity);
        when(pointOfSaleRepositoryMock.findDuplicate(MERCHANT_ID, entity)).thenReturn(Optional.empty());
        when(pointOfSaleRepositoryMock.save(entity)).thenReturn(entity);
        doThrow(new RuntimeException("DB error"))
                .when(pointOfSalesInitiativeRepositoryMock).saveAll(anySet());

        when(keycloakServiceMock.getUserResource()).thenReturn(usersResourceMock);
        when(usersResourceMock.searchByEmail(entity.getContactEmail(), true)).thenReturn(List.of(userRep));
        when(usersResourceMock.get("KC-USER-1")).thenReturn(userResourceMock);

        assertThrows(ServiceException.class,
                () -> service.savePointOfSales(MERCHANT_ID, INITIATIVE_ID, List.of(dto)));

        verify(keycloakServiceMock).manageReferentUserOnKeycloak(eq(entity), eq(dto.getContactEmail()));
        verify(pointOfSalesInitiativeRepositoryMock)
                .deleteByMerchantIdAndInitiativeIdAndPointOfSaleIdIn(MERCHANT_ID, INITIATIVE_ID, List.of("POS-1"));
        verify(pointOfSaleRepositoryMock).deleteById("POS-1");
        verify(keycloakServiceMock).getUserResource();
        verify(usersResourceMock).searchByEmail(entity.getContactEmail(), true);
        verify(usersResourceMock).get("KC-USER-1");
        verify(userResourceMock).remove();
    }

    @Test
    void shouldContinueCompensation_whenDeleteThrows() {
        PointOfSaleDTO dto = dto();
        PointOfSale entity = entity("POS-1");

        doNothing().when(merchantServiceMock).verifyMerchantExists(MERCHANT_ID);
        when(pointOfSaleDTOMapperMock.dtoToEntity(dto, MERCHANT_ID)).thenReturn(entity);
        when(pointOfSaleRepositoryMock.findDuplicate(MERCHANT_ID, entity)).thenReturn(Optional.empty());
        when(pointOfSaleRepositoryMock.save(entity)).thenReturn(entity);
        doThrow(new RuntimeException("DB error"))
                .when(pointOfSalesInitiativeRepositoryMock).saveAll(anySet());

        doThrow(new RuntimeException("delete failed"))
                .when(pointOfSaleRepositoryMock).deleteById("POS-1");

        assertThrows(ServiceException.class,
                () -> service.savePointOfSales(MERCHANT_ID, INITIATIVE_ID, List.of(dto)));

        verify(keycloakServiceMock).manageReferentUserOnKeycloak(eq(entity), eq(dto.getContactEmail()));
        verify(pointOfSalesInitiativeRepositoryMock)
                .deleteByMerchantIdAndInitiativeIdAndPointOfSaleIdIn(MERCHANT_ID, INITIATIVE_ID, List.of("POS-1"));
        verify(pointOfSaleRepositoryMock).deleteById("POS-1");
        verify(keycloakServiceMock, never()).getUserResource();
    }
}