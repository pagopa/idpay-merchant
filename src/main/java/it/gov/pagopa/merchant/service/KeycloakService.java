package it.gov.pagopa.merchant.service;

import it.gov.pagopa.merchant.model.PointOfSale;
import org.keycloak.admin.client.resource.UsersResource;

public interface KeycloakService {
    void manageReferentUserOnKeycloak(PointOfSale pointOfSale, String oldEmail);
    UsersResource getUserResource();
}
