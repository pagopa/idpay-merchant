package it.gov.pagopa.merchant.service.pointofsales;

import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.merchant.constants.PointOfSaleConstants;
import it.gov.pagopa.merchant.dto.enums.PointOfSaleTypeEnum;
import it.gov.pagopa.merchant.dto.pointofsales.PointOfSaleDTO;
import it.gov.pagopa.merchant.exception.custom.PosValidationException;
import it.gov.pagopa.merchant.mapper.PointOfSaleDTOMapper;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.model.PointOfSalesInitiative;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.repository.PointOfSalesInitiativeRepository;
import it.gov.pagopa.merchant.service.KeycloakService;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleDTOFaker;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleFaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointOfSaleWriterTest {

    @Mock
    MerchantService merchantService;
    @Mock
    PointOfSaleRepository repository;
    @Mock
    KeycloakService keycloakService;
    @Mock
    PointOfSaleDTOMapper mapper;
    @Mock
    PointOfSalesInitiativeRepository initiativeRepository;

    PointOfSaleWriterImpl service;

    static final String M = "M1";
    static final String I = "I1";

    @BeforeEach
    void setUp() {
        service = new PointOfSaleWriterImpl(
                merchantService,
                repository,
                keycloakService,
                mapper,
                initiativeRepository
        );
    }

    private PointOfSaleDTO dto() {
        return PointOfSaleDTOFaker.mockInstance();
    }

    private PointOfSale entity(String id) {
        PointOfSale p = PointOfSaleFaker.mockInstance();
        p.setId(id);
        return p;
    }


    @Test
    void shouldSaveSuccessfully () {
        PointOfSaleDTO dto = dto();
        PointOfSale entity = entity("P1");
        List<PointOfSaleDTO> list = List.of(dto);

        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.findByContactEmail(any())).thenReturn(Optional.empty());
        when(repository.findDuplicate(entity)).thenReturn(Optional.empty());
        when(repository.save(entity)).thenReturn(entity);

        service.savePointOfSales(M, I, list);

        verify(repository).save(entity);
        verify(keycloakService).manageReferentUserOnKeycloak(entity, dto.getContactEmail());
        verify(initiativeRepository).saveAll(any());
    }

    @Test
    void shouldFail_whenEmailAlreadyExists () {
        PointOfSaleDTO dto = dto();
        PointOfSale existing = entity("X");
        List<PointOfSaleDTO> list = List.of(dto);

        when(mapper.dtoToEntity(dto, M)).thenReturn(existing);
        when(repository.findByContactEmail(any())).thenReturn(Optional.of(existing));

        PosValidationException ex = assertThrows(
                PosValidationException.class,
                () -> service.savePointOfSales(M, I, list)
        );

        assertFalse(ex.getErrors().isEmpty());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldFail_whenOnlinePosAlreadyOnSameInitiative () {
        PointOfSaleDTO dto = dto();
        dto.setType(PointOfSaleTypeEnum.ONLINE);
        dto.setWebsite("https://test.it");

        PointOfSale entity = entity("P1");
        List<PointOfSaleDTO> list = List.of(dto);

        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.findByContactEmail(any())).thenReturn(Optional.empty());
        when(repository.findDuplicate(entity)).thenReturn(Optional.of(entity));

        when(initiativeRepository
                .findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue(
                        entity.getId(), I, M))
                .thenReturn(Optional.of(new PointOfSalesInitiative()));

        PosValidationException ex = assertThrows(
                PosValidationException.class,
                () -> service.savePointOfSales(M, I, list)
        );

        assertTrue(ex.getErrors().stream().anyMatch(e ->
                e.getCode().equals(PointOfSaleConstants.CODE_ONLINE_POS_ALREADY_REGISTERED)));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldFail_whenPhysicalPosAlreadyOnSameInitiative () {
        PointOfSaleDTO dto = dto();
        dto.setType(PointOfSaleTypeEnum.PHYSICAL);
        dto.setAddress("Via Roma");

        PointOfSale entity = entity("P2");
        List<PointOfSaleDTO> list = List.of(dto);

        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.findByContactEmail(any())).thenReturn(Optional.empty());
        when(repository.findDuplicate(entity)).thenReturn(Optional.of(entity));

        when(initiativeRepository
                .findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue(
                        entity.getId(), I, M))
                .thenReturn(Optional.of(new PointOfSalesInitiative()));

        PosValidationException ex = assertThrows(
                PosValidationException.class,
                () -> service.savePointOfSales(M, I, list)
        );

        assertTrue(ex.getErrors().stream().anyMatch(e ->
                e.getCode().equals(PointOfSaleConstants.CODE_PHYSICAL_POS_ALREADY_REGISTERED)));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldFail_whenPosAlreadyOnAnotherInitiative () {
        PointOfSaleDTO dto = dto();
        PointOfSale entity = entity("P3");
        List<PointOfSaleDTO> list = List.of(dto);

        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.findByContactEmail(any())).thenReturn(Optional.empty());
        when(repository.findDuplicate(entity)).thenReturn(Optional.of(entity));

        when(initiativeRepository
                .findByPointOfSaleIdAndInitiativeIdAndMerchantIdAndEnabledTrue(
                        entity.getId(), I, M))
                .thenReturn(Optional.empty());

        PosValidationException ex = assertThrows(
                PosValidationException.class,
                () -> service.savePointOfSales(M, I, list)
        );

        assertTrue(ex.getErrors().stream().anyMatch(e ->
                e.getCode().equals(PointOfSaleConstants.CODE_POS_ALREADY_REGISTERED_OTHER_INITIATIVE)));
    }

    @Test
    void shouldReturnMultipleErrors () {
        PointOfSaleDTO dto1 = dto();
        PointOfSaleDTO dto2 = dto();

        PointOfSale e1 = entity("1");
        PointOfSale e2 = entity("2");

        List<PointOfSaleDTO> list = List.of(dto1, dto2);

        when(mapper.dtoToEntity(dto1, M)).thenReturn(e1);
        when(mapper.dtoToEntity(dto2, M)).thenReturn(e2);

        when(repository.findByContactEmail(any()))
                .thenReturn(Optional.of(e1));

        assertThrows(
                PosValidationException.class,
                () -> service.savePointOfSales(M, I, list)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowServiceException_onDuplicateKey () {
        PointOfSaleDTO dto = dto();
        PointOfSale entity = entity("P1");
        List<PointOfSaleDTO> list = List.of(dto);

        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.findByContactEmail(any())).thenReturn(Optional.empty());
        when(repository.findDuplicate(entity)).thenReturn(Optional.empty());
        when(repository.save(entity)).thenThrow(new DuplicateKeyException("dup"));

        assertThrows(ServiceException.class,
                () -> service.savePointOfSales(M, I, list));
    }

    @Test
    void shouldThrowServiceException_onGenericError () {
        PointOfSaleDTO dto = dto();
        PointOfSale entity = entity("P1");
        List<PointOfSaleDTO> list = List.of(dto);

        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.findDuplicate(entity)).thenReturn(Optional.empty());
        when(repository.save(entity)).thenThrow(new RuntimeException());

        assertThrows(ServiceException.class,
                () -> service.savePointOfSales(M, I, list));
    }

    @Test
    void shouldCompensate_whenAssociationSaveFails(){
        PointOfSaleDTO dto = dto();
        dto.setContactEmail("test@email.it");

        PointOfSale entity = entity("P1");
        entity.setContactEmail("test@email.it");

        List<PointOfSaleDTO> list = List.of(dto);

        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.findByContactEmail(any())).thenReturn(Optional.empty());
        when(repository.findDuplicate(entity)).thenReturn(Optional.empty());
        when(repository.save(entity)).thenReturn(entity);

        doThrow(new RuntimeException())
                .when(initiativeRepository).saveAll(any());

        UsersResource usersResource = mock(UsersResource.class);
        when(keycloakService.getUserResource()).thenReturn(usersResource);

        UserRepresentation user = new UserRepresentation();
        user.setId("USER_ID");

        var userResource = mock(org.keycloak.admin.client.resource.UserResource.class);

        when(usersResource.searchByEmail("test@email.it", true))
                .thenReturn(List.of(user));

        when(usersResource.get("USER_ID"))
                .thenReturn(userResource);

        assertThrows(ServiceException.class,
                () -> service.savePointOfSales(M, I, list));

        verify(initiativeRepository)
                .deleteByMerchantIdAndInitiativeIdAndPointOfSaleIdIn(eq(M), eq(I), any());

        verify(repository).deleteById("P1");

        verify(usersResource).searchByEmail("test@email.it", true);
        verify(userResource).remove();
    }



    @Test
    void shouldContinueCompensation_whenKeycloakFails() {

        PointOfSaleDTO dto = dto();
        PointOfSale entity = entity("P1");
        List<PointOfSaleDTO> list = List.of(dto);

        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.findByContactEmail(any())).thenReturn(Optional.empty());
        when(repository.findDuplicate(entity)).thenReturn(Optional.empty());
        when(repository.save(entity)).thenReturn(entity);

        doThrow(new RuntimeException())
                .when(initiativeRepository).saveAll(any());

        UsersResource usersResource = mock(UsersResource.class);
        when(keycloakService.getUserResource()).thenReturn(usersResource);

        when(usersResource.searchByEmail(anyString(), eq(true)))
                .thenThrow(new RuntimeException());

        assertThrows(ServiceException.class,
                () -> service.savePointOfSales(M, I, list));


        verify(repository).deleteById(entity.getId());
    }

    @Test
    void shouldContinueCompensation_whenDeleteFails() {
        PointOfSaleDTO dto = dto();
        PointOfSale entity = entity("P1");

        dto.setContactEmail("test@email.it");
        entity.setContactEmail("test@email.it");

        List<PointOfSaleDTO> list = List.of(dto);

        when(mapper.dtoToEntity(dto, M)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);

        doThrow(new RuntimeException())
                .when(initiativeRepository).saveAll(any());

        doThrow(new RuntimeException())
                .when(repository).deleteById("P1");


        assertThrows(ServiceException.class,
                () -> service.savePointOfSales(M, I, list));

        verify(repository).deleteById("P1");
    }


}
