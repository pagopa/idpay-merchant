package it.gov.pagopa.merchant.service;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.merchant.model.PointOfSale;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.gov.pagopa.merchant.utils.Utilities.sanitizeForLog;

@Slf4j
@Service
public class KeycloakServiceImpl implements KeycloakService {

    private final Keycloak keycloakAdminClient;
    private final String realm;
    private final String redirectURI;
    private final String keycloakClientId;
    private final Integer keycloakUserActionsEmailLifespan;

    private static final String REQUIRED_ACTION_UPDATE_PASSWORD = "UPDATE_PASSWORD";

    public KeycloakServiceImpl( Keycloak keycloakAdminClient,
                                  @Value("${keycloak.admin.realm}") String realm,
                                  @Value("${keycloak.admin.user.actions.email.lifespan}") Integer keycloakUserActionsEmailLifespan,
                                  @Value("${keycloak.admin.redirect-uri}") String redirectURI,
                                  @Value("${keycloak.admin.redirect-client-id}") String keycloakClientId) {
        this.keycloakAdminClient = keycloakAdminClient;
        this.realm = realm;
        this.redirectURI = redirectURI;
        this.keycloakClientId = keycloakClientId;
        this.keycloakUserActionsEmailLifespan = keycloakUserActionsEmailLifespan;
    }

    @Override
    public void manageReferentUserOnKeycloak(PointOfSale pointOfSale, String oldEmail) {
        final String contactEmail = pointOfSale.getContactEmail();

        if (StringUtils.isEmpty(contactEmail)) {
            log.warn(
                    "[KEYCLOAK] Point of Sale with ID {} for merchant {} has no contact email. Skipping Keycloak user creation.",
                    sanitizeForLog(pointOfSale.getId()), sanitizeForLog(pointOfSale.getMerchantId()));
            return;
        }

        UsersResource usersResource = keycloakAdminClient.realm(realm).users();

        try {
            deleteOldUser(usersResource, oldEmail, contactEmail);
            handleNewOrExistingUser(usersResource, pointOfSale, contactEmail);
        } catch (Exception e) {
            log.error(
                    "[KEYCLOAK] Error while creating Keycloak user for Point of Sale with ID {}. Exception: {}",
                    pointOfSale.getId(), e.getMessage(), e);
        }
    }

    @Override
    public UsersResource getUserResource(){
        return keycloakAdminClient.realm(realm).users();
    }
    private void deleteOldUser(UsersResource usersResource, String oldEmail, String newEmail) {
        if (StringUtils.isNotEmpty(oldEmail) && !oldEmail.equalsIgnoreCase(newEmail)) {
            List<UserRepresentation> existingUsers = usersResource.searchByEmail(oldEmail, true);
            for (UserRepresentation user : existingUsers) {
                usersResource.get(user.getId()).logout();
                usersResource.get(user.getId()).remove();
                log.info("[KEYCLOAK] Logged out and deleted user with email: {}", oldEmail);
            }
        }
    }

    private void handleNewOrExistingUser(UsersResource usersResource, PointOfSale pointOfSale,
                                         String contactEmail) {
        List<UserRepresentation> existingUsers = usersResource.searchByEmail(contactEmail, true);

        if (existingUsers.isEmpty()) {
            createNewUserAndSendActionsEmail(usersResource, pointOfSale);
        } else {
            updateEnabledUsers(usersResource, pointOfSale, contactEmail, existingUsers);
            log.info(
                    "[KEYCLOAK] User already exists and is enabled. The new Point of Sale with ID {} will be associated with the existing user.",
                    sanitizeForLog(pointOfSale.getId()));
        }
    }

    private void updateEnabledUsers(UsersResource usersResource, PointOfSale pointOfSale,
                                    String contactEmail, List<UserRepresentation> users) {
        for (UserRepresentation user : users) {
            user.setFirstName(pointOfSale.getContactName());
            user.setLastName(pointOfSale.getContactSurname());
            usersResource.get(user.getId()).update(user);
            log.info("[KEYCLOAK] Updated contact name/surname for existing enabled user with email: {}",
                    sanitizeForLog(contactEmail));
        }
    }


    private void createNewUserAndSendActionsEmail(UsersResource usersResource,
                                                  PointOfSale pointOfSale) {
        UserRepresentation newUser = new UserRepresentation();
        newUser.setEmail(pointOfSale.getContactEmail());
        newUser.setUsername(pointOfSale.getContactEmail());
        newUser.setFirstName(pointOfSale.getContactName());
        newUser.setLastName(pointOfSale.getContactSurname());

        newUser.setEnabled(true);
        newUser.setEmailVerified(true);

        // Custom attrs
        Map<String, List<String>> attrs = new HashMap<>();
        if(StringUtils.isNotEmpty(pointOfSale.getMerchantId())){
            attrs.put("merchantId", List.of(pointOfSale.getMerchantId()));
        }
        if(StringUtils.isNotEmpty(pointOfSale.getId())){
            attrs.put("pointOfSaleId", List.of(pointOfSale.getId()));
        }
        newUser.setAttributes(attrs);

        log.info("[KEYCLOAK] Attempting to create a new Keycloak user linked to Point of Sale ID {}",
                sanitizeForLog(pointOfSale.getId()));

        try (Response response = usersResource.create(newUser)) {
            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) { // Status code 201
                String userId = CreatedResponseUtil.getCreatedId(response);
                log.info("[KEYCLOAK] User created successfully with ID {}. Sending password setup email.",
                        userId);

                // The action "UPDATE_PASSWORD" sends an email with a link that will expire after the lifespan to reset the user password
                usersResource.get(userId)
                        .executeActionsEmail(keycloakClientId, redirectURI, keycloakUserActionsEmailLifespan,
                                List.of(REQUIRED_ACTION_UPDATE_PASSWORD));

            } else {
                // Handling non-success cases with a log
                log.error("[KEYCLOAK] Failed to create Keycloak user. Status: {}, Reason: {}.",
                        response.getStatus(), response.getStatusInfo().getReasonPhrase());
            }
        } catch (Exception e) {
            log.error("[KEYCLOAK] An exception occurred while creating Keycloak user.", e);
            throw e;
        }
    }
}
