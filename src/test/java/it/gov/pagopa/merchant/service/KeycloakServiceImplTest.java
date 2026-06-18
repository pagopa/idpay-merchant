package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.model.PointOfSale;
import it.gov.pagopa.merchant.test.fakers.PointOfSaleFaker;
import jakarta.ws.rs.core.Response;
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakServiceImplTest {

    @Mock
    private Keycloak keycloakAdminClientMock;
    @Mock
    private RealmResource realmResourceMock;
    @Mock
    private UsersResource usersResourceMock;
    @Mock
    private UserResource userResourceMock;
    @Mock
    private Response responseMock;

    private static final String REALM        = "test-realm";
    private static final String REDIRECT_URI = "http://localhost/redirect";
    private static final String CLIENT_ID    = "test-client";
    private static final Integer LIFESPAN    = 86400;

    private KeycloakServiceImpl keycloakService;

    @BeforeEach
    void setUp() {
        keycloakService = new KeycloakServiceImpl(
                keycloakAdminClientMock,
                REALM,
                LIFESPAN,
                REDIRECT_URI,
                CLIENT_ID
        );
    }

    @Test
    void getUserResource_returnsUsersResource() {
        when(keycloakAdminClientMock.realm(REALM)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);

        UsersResource result = keycloakService.getUserResource();

        assertNotNull(result);
        verify(keycloakAdminClientMock).realm(REALM);
        verify(realmResourceMock).users();
    }

    @Test
    void manageReferentUserOnKeycloak_noContactEmail_skipsKeycloak() {
        PointOfSale pos = PointOfSaleFaker.mockInstance();
        pos.setContactEmail(null);

        keycloakService.manageReferentUserOnKeycloak(pos, null);

        verify(keycloakAdminClientMock, never()).realm(anyString());
    }

    @Test
    void manageReferentUserOnKeycloak_newUser_createsUserAndSendsEmail() {
        PointOfSale pos = PointOfSaleFaker.mockInstance();
        pos.setContactEmail("new.user@example.com");

        when(keycloakAdminClientMock.realm(REALM)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.searchByEmail(pos.getContactEmail(), true))
                .thenReturn(new ArrayList<>());
        when(usersResourceMock.create(any(UserRepresentation.class))).thenReturn(responseMock);
        when(responseMock.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(responseMock.getStatusInfo()).thenReturn(Response.Status.CREATED);
        when(responseMock.getLocation()).thenReturn(URI.create("users/NEW_USER_ID"));
        when(usersResourceMock.get("NEW_USER_ID")).thenReturn(userResourceMock);
        doNothing().when(userResourceMock)
                .executeActionsEmail(anyString(), anyString(), anyInt(), any());

        keycloakService.manageReferentUserOnKeycloak(pos, null);

        verify(usersResourceMock).create(any(UserRepresentation.class));
        verify(userResourceMock).executeActionsEmail(
                eq(CLIENT_ID), eq(REDIRECT_URI), eq(LIFESPAN),
                Mockito.eq(List.of("UPDATE_PASSWORD")));
    }

    @Test
    void manageReferentUserOnKeycloak_userAlreadyExists_updatesNameSurnameOnly() {
        PointOfSale pos = PointOfSaleFaker.mockInstance();
        pos.setContactEmail("existing.user@example.com");
        pos.setContactName("NewName");
        pos.setContactSurname("NewSurname");

        UserRepresentation existingUser = new UserRepresentation();
        existingUser.setId("existing-user-id");
        existingUser.setEmail(pos.getContactEmail());
        existingUser.setEnabled(true);

        when(keycloakAdminClientMock.realm(REALM)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.searchByEmail(pos.getContactEmail(), true))
                .thenReturn(List.of(existingUser));
        when(usersResourceMock.get("existing-user-id")).thenReturn(userResourceMock);
        doNothing().when(userResourceMock).update(any());

        keycloakService.manageReferentUserOnKeycloak(pos, null);

        verify(userResourceMock).update(argThat(user ->
                "NewName".equals(user.getFirstName()) &&
                        "NewSurname".equals(user.getLastName())
        ));
        verify(usersResourceMock, never()).create(any(UserRepresentation.class));
        verify(userResourceMock, never())
                .executeActionsEmail(anyString(), anyString(), anyInt(), anyList());
    }

    @Test
    void manageReferentUserOnKeycloak_keycloakCreationFails_logsErrorAndDoesNotSendEmail() {
        PointOfSale pos = PointOfSaleFaker.mockInstance();
        pos.setContactEmail("failed.user@example.com");

        when(keycloakAdminClientMock.realm(REALM)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.searchByEmail(pos.getContactEmail(), true))
                .thenReturn(new ArrayList<>());
        when(usersResourceMock.create(any(UserRepresentation.class))).thenReturn(responseMock);
        when(responseMock.getStatus()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode());
        when(responseMock.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);

        keycloakService.manageReferentUserOnKeycloak(pos, null);

        verify(usersResourceMock).create(any(UserRepresentation.class));
        verify(usersResourceMock, never()).get(anyString());
        verify(userResourceMock, never())
                .executeActionsEmail(anyString(), anyString(), anyInt(), anyList());
    }

    @Test
    void manageReferentUserOnKeycloak_emailChanged_deletesOldUserAndCreatesNew() {
        PointOfSale pos = PointOfSaleFaker.mockInstance();
        pos.setContactEmail("new.email@example.com");

        String oldEmail = "old.email@example.com";

        UserRepresentation oldUser = new UserRepresentation();
        oldUser.setId("old-user-id");
        oldUser.setEmail(oldEmail);

        when(keycloakAdminClientMock.realm(REALM)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);

        when(usersResourceMock.searchByEmail(oldEmail, true)).thenReturn(List.of(oldUser));
        when(usersResourceMock.get("old-user-id")).thenReturn(userResourceMock);
        doNothing().when(userResourceMock).logout();
        doNothing().when(userResourceMock).remove();

        when(usersResourceMock.searchByEmail("new.email@example.com", true))
                .thenReturn(new ArrayList<>());
        when(usersResourceMock.create(any(UserRepresentation.class))).thenReturn(responseMock);
        when(responseMock.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
        when(responseMock.getStatusInfo()).thenReturn(Response.Status.CREATED);
        when(responseMock.getLocation()).thenReturn(URI.create("users/NEW_USER_ID"));
        when(usersResourceMock.get("NEW_USER_ID")).thenReturn(userResourceMock);
        doNothing().when(userResourceMock)
                .executeActionsEmail(anyString(), anyString(), anyInt(), any());

        keycloakService.manageReferentUserOnKeycloak(pos, oldEmail);

        verify(userResourceMock).logout();
        verify(userResourceMock).remove();
        verify(usersResourceMock).create(any(UserRepresentation.class));
    }

    @Test
    void manageReferentUserOnKeycloak_emailUnchanged_doesNotDeleteOldUser() {
        String sameEmail = "same.email@example.com";

        PointOfSale pos = PointOfSaleFaker.mockInstance();
        pos.setContactEmail(sameEmail);

        UserRepresentation existingUser = new UserRepresentation();
        existingUser.setId("user-id");
        existingUser.setEmail(sameEmail);

        when(keycloakAdminClientMock.realm(REALM)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.searchByEmail(sameEmail, true)).thenReturn(List.of(existingUser));
        when(usersResourceMock.get("user-id")).thenReturn(userResourceMock);
        doNothing().when(userResourceMock).update(any());

        keycloakService.manageReferentUserOnKeycloak(pos, sameEmail);

        verify(userResourceMock, never()).logout();
        verify(userResourceMock, never()).remove();
    }

    @Test
    void manageReferentUserOnKeycloak_keycloakThrowsException_doesNotPropagate() {
        PointOfSale pos = PointOfSaleFaker.mockInstance();
        pos.setContactEmail("error.user@example.com");

        when(keycloakAdminClientMock.realm(REALM)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.searchByEmail(pos.getContactEmail(), true))
                .thenThrow(new RuntimeException("Keycloak connection error"));

        keycloakService.manageReferentUserOnKeycloak(pos, null);

        verify(usersResourceMock, never()).create(any());
    }

    @Test
    void manageReferentUserOnKeycloak_multipleExistingUsers_allUpdated() {
        PointOfSale pos = PointOfSaleFaker.mockInstance();
        pos.setContactEmail("multi.user@example.com");
        pos.setContactName("Multi");
        pos.setContactSurname("User");

        UserRepresentation user1 = new UserRepresentation();
        user1.setId("uid-1");
        UserRepresentation user2 = new UserRepresentation();
        user2.setId("uid-2");

        when(keycloakAdminClientMock.realm(REALM)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(usersResourceMock.searchByEmail(pos.getContactEmail(), true))
                .thenReturn(List.of(user1, user2));
        when(usersResourceMock.get("uid-1")).thenReturn(userResourceMock);
        when(usersResourceMock.get("uid-2")).thenReturn(userResourceMock);
        doNothing().when(userResourceMock).update(any());

        keycloakService.manageReferentUserOnKeycloak(pos, null);

        verify(userResourceMock, times(2)).update(any(UserRepresentation.class));
        verify(usersResourceMock, never()).create(any());
    }
}