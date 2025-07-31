package it.gov.pagopa.merchant.service.pointofsales;

import com.mongodb.MongoException;
import it.gov.pagopa.common.web.exception.ServiceException;
import it.gov.pagopa.merchant.dto.MerchantDetailDTO;
import it.gov.pagopa.merchant.exception.custom.MerchantNotFoundException;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleDuplicateException;
import it.gov.pagopa.merchant.exception.custom.PointOfSaleNotFoundException;
import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.repository.PointOfSaleRepository;
import it.gov.pagopa.merchant.service.MerchantService;
import it.gov.pagopa.merchant.test.fakers.MerchantDetailDTOFaker;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleFaker;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointOfSaleServiceTest {

  @Mock private MerchantService merchantServiceMock;
  @Mock private PointOfSaleRepository repositoryMock;
  @Mock private Keycloak keycloak;

  @Mock private RealmResource realmResourceMock;
  @Mock private UsersResource usersResourceMock;
  @Mock private UserResource userResourceMock;
  @Mock private Response responseMock;

  private static final String MERCHANT_ID = "MERCHANT_ID";

  PointOfSaleService service;

  @BeforeEach
  void setUp() {
    service = new PointOfSaleServiceImpl(
        merchantServiceMock,
        repositoryMock,
        keycloak,
        "test-realm");
  }

  @Test
  void savePointOfSalesOK(){
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);
    when(repositoryMock.save(any())).thenReturn(pointOfSale);
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale));
    service.savePointOfSales(MERCHANT_ID,List.of(pointOfSale));

    Mockito.verify(repositoryMock, Mockito.times(1)).save(pointOfSale);
  }

  @Test
  void savePointOfSalesOK_withIdNull(){
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId(null);
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);
    when(repositoryMock.save(any())).thenReturn(pointOfSale);
    service.savePointOfSales(MERCHANT_ID,List.of(pointOfSale));

    Mockito.verify(repositoryMock, Mockito.times(1)).save(pointOfSale);

  }


  @Test
  void savePointOfSalesOK_withId(){
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId(new ObjectId());
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);
    when(repositoryMock.save(any())).thenReturn(pointOfSale);
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale));
    service.savePointOfSales(MERCHANT_ID,List.of(pointOfSale));

    Mockito.verify(repositoryMock, Mockito.times(1)).save(pointOfSale);

  }

  @Test
  void savePointOfSalesOK_withIdNotFound(){
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setId(new ObjectId());
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);
    when(repositoryMock.findById(any())).thenReturn(Optional.empty());
    List<PointOfSale> pointOfSales = new ArrayList<>();
    pointOfSales.add(pointOfSale);
    assertThrows(PointOfSaleNotFoundException.class, () -> callSave(pointOfSales));
  }


  @Test
  void savePointOfSalesKO_genericError(){
    PointOfSale pointOfSale1 = PointOfSaleFaker.mockInstance();
    PointOfSale pointOfSale2 = PointOfSaleFaker.mockInstance();
    List<PointOfSale> pointOfSales = new ArrayList<>();
    pointOfSales.add(pointOfSale1);
    pointOfSales.add(pointOfSale2);
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale1));
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale2));
    when(repositoryMock.save(pointOfSale1)).thenReturn(pointOfSale1);
    when(repositoryMock.save(pointOfSale2)).thenThrow(new MongoException("DUMMY_EXCEPTION"));
    Mockito.doThrow(new MongoException("Command error dummy")).when(repositoryMock).deleteById(any());
    assertThrows(ServiceException.class, () -> callSave(pointOfSales));
  }

  @Test
  void savePointOfSalesKO_genericErrorCompensatingOk(){
    PointOfSale pointOfSale1 = PointOfSaleFaker.mockInstance();
    PointOfSale pointOfSale2 = PointOfSaleFaker.mockInstance();
    List<PointOfSale> pointOfSales = new ArrayList<>();
    pointOfSales.add(pointOfSale1);
    pointOfSales.add(pointOfSale2);
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale1));
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale2));
    when(repositoryMock.save(pointOfSale1)).thenReturn(pointOfSale1);
    when(repositoryMock.save(pointOfSale2)).thenThrow(new MongoException("DUMMY_EXCEPTION"));
    doNothing().when(repositoryMock).deleteById(any());
    assertThrows(ServiceException.class, () -> callSave(pointOfSales));
  }

  @Test
  void savePointOfSalesKO_duplicateKeyException(){
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale));
    DuplicateKeyException duplicateKeyException = mock(DuplicateKeyException.class);
    when(repositoryMock.save(any())).thenThrow(duplicateKeyException);
    assertThrows(PointOfSaleDuplicateException.class, () -> callSave(pointOfSale));
  }

  @Test
  void savePointOfSalesKO_merchantIsNull(){
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(null);

    assertThrows(MerchantNotFoundException.class, () -> callSave(pointOfSale));
  }


  private void callSave(List<PointOfSale> dtos){
    service.savePointOfSales(MERCHANT_ID,dtos);
  }

  private void callSave(PointOfSale dto){
    service.savePointOfSales(MERCHANT_ID,List.of(dto));
  }


  @Test
  void getPointOfSalesListOK(){
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    MerchantDetailDTO merchantDetailDTOFaker = MerchantDetailDTOFaker.mockInstance(1);
    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTOFaker);

    Criteria criteria = new Criteria();
    when(repositoryMock.getCriteria(any(),any(),any(),any(),any())).thenReturn(criteria);
    when(repositoryMock.findByFilter(any(),any())).thenReturn(List.of(pointOfSale));

    Pageable paging = PageRequest.of(0, 20, Sort.by("franchiseName"));
    Page<PointOfSale> pointOfSalePage = service.getPointOfSalesList(MERCHANT_ID,"type","city","address","contactName",paging);
    assertNotNull(pointOfSalePage);
  }

  @Test
  void savePointOfSales_keycloakUserCreationSuccess() {
    // Given
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setContactEmail("new.user@example.com");
    MerchantDetailDTO merchantDetailDTO = MerchantDetailDTOFaker.mockInstance(1);

    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTO);
    when(repositoryMock.save(any(PointOfSale.class))).thenReturn(pointOfSale);
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale));

    // Mock Keycloak interactions
    when(keycloak.realm(anyString())).thenReturn(realmResourceMock);
    when(realmResourceMock.users()).thenReturn(usersResourceMock);
    when(usersResourceMock.searchByEmail(pointOfSale.getContactEmail(), true)).thenReturn(new ArrayList<>());
    when(usersResourceMock.create(any(UserRepresentation.class))).thenReturn(responseMock);
    when(responseMock.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
    when(responseMock.getLocation()).thenReturn(URI.create("users/DUMMY_USER_ID"));
    when(usersResourceMock.get(anyString())).thenReturn(userResourceMock);
    doNothing().when(userResourceMock).executeActionsEmail(any(), anyInt());

    // When
    service.savePointOfSales(MERCHANT_ID, List.of(pointOfSale));

    // Then
    Mockito.verify(repositoryMock).save(pointOfSale);
    Mockito.verify(usersResourceMock).create(any(UserRepresentation.class));
    Mockito.verify(userResourceMock).executeActionsEmail(Mockito.eq(List.of("UPDATE_PASSWORD")), anyInt());
  }

  @Test
  void savePointOfSales_keycloakUserAlreadyExists() {
    // Given
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setContactEmail("existing.user@example.com");
    MerchantDetailDTO merchantDetailDTO = MerchantDetailDTOFaker.mockInstance(1);
    UserRepresentation existingUser = new UserRepresentation();
    existingUser.setEmail(pointOfSale.getContactEmail());

    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTO);
    when(repositoryMock.save(any(PointOfSale.class))).thenReturn(pointOfSale);
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale));

    // Mock Keycloak interactions
    when(keycloak.realm(anyString())).thenReturn(realmResourceMock);
    when(realmResourceMock.users()).thenReturn(usersResourceMock);
    when(usersResourceMock.searchByEmail(pointOfSale.getContactEmail(), true)).thenReturn(List.of(existingUser));

    // When
    service.savePointOfSales(MERCHANT_ID, List.of(pointOfSale));

    // Then
    Mockito.verify(repositoryMock).save(pointOfSale);
    Mockito.verify(usersResourceMock, Mockito.never()).create(any(UserRepresentation.class));
    Mockito.verify(userResourceMock, Mockito.never()).executeActionsEmail(any(), anyInt());
  }

  @Test
  void savePointOfSales_noContactEmail() {
    // Given
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setContactEmail(null); // No email
    MerchantDetailDTO merchantDetailDTO = MerchantDetailDTOFaker.mockInstance(1);

    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTO);
    when(repositoryMock.save(any(PointOfSale.class))).thenReturn(pointOfSale);
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale));

    // When
    service.savePointOfSales(MERCHANT_ID, List.of(pointOfSale));

    // Then
    Mockito.verify(repositoryMock).save(pointOfSale);
    Mockito.verify(keycloak, Mockito.never()).realm(anyString());
  }

  @Test
  void savePointOfSales_keycloakUserCreationFails() {
    // Given
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setContactEmail("failed.user@example.com");
    MerchantDetailDTO merchantDetailDTO = MerchantDetailDTOFaker.mockInstance(1);

    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTO);
    when(repositoryMock.save(any(PointOfSale.class))).thenReturn(pointOfSale);
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale));

    // Mock Keycloak interactions for failure
    when(keycloak.realm(anyString())).thenReturn(realmResourceMock);
    when(realmResourceMock.users()).thenReturn(usersResourceMock);
    when(usersResourceMock.searchByEmail(pointOfSale.getContactEmail(), true)).thenReturn(new ArrayList<>());
    when(usersResourceMock.create(any(UserRepresentation.class))).thenReturn(responseMock);
    when(responseMock.getStatus()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode()); // Simulate failure
    when(responseMock.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);
    when(responseMock.readEntity(String.class)).thenReturn("Error details");


    // When
    service.savePointOfSales(MERCHANT_ID, List.of(pointOfSale));

    // Then
    Mockito.verify(repositoryMock).save(pointOfSale);
    Mockito.verify(usersResourceMock).create(any(UserRepresentation.class));
    Mockito.verify(usersResourceMock, Mockito.never()).get(anyString());
  }

  @Test
  void savePointOfSales_keycloakThrowsException_triggersRollback() {
    // Given
    PointOfSale pointOfSale = PointOfSaleFaker.mockInstance();
    pointOfSale.setContactEmail("exception.user@example.com");
    MerchantDetailDTO merchantDetailDTO = MerchantDetailDTOFaker.mockInstance(1);

    when(merchantServiceMock.getMerchantDetail(anyString())).thenReturn(merchantDetailDTO);
    when(repositoryMock.save(any(PointOfSale.class))).thenReturn(pointOfSale);
    when(repositoryMock.findById(any())).thenReturn(Optional.of(pointOfSale));

    // Mock Keycloak to throw an exception
    when(keycloak.realm(anyString())).thenReturn(realmResourceMock);
    when(realmResourceMock.users()).thenReturn(usersResourceMock);
    when(usersResourceMock.searchByEmail(pointOfSale.getContactEmail(), true)).thenReturn(new ArrayList<>());
    when(usersResourceMock.create(any(UserRepresentation.class))).thenThrow(new jakarta.ws.rs.ProcessingException("Keycloak connection failed"));

    // When & Then
    assertThrows(ServiceException.class, () -> service.savePointOfSales(MERCHANT_ID, List.of(pointOfSale)));

    // Verify compensation logic was triggered
    Mockito.verify(repositoryMock).deleteById(pointOfSale.getId().toString());
  }
}