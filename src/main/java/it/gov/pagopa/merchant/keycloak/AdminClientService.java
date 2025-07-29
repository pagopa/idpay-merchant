package it.gov.pagopa.merchant.keycloak;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AdminClientService {

    private final Keycloak keycloak;

    public AdminClientService(Keycloak keycloak){
        this.keycloak = keycloak;
    }

    public List<UserRepresentation> searchByUsername(String username, boolean exact, String realm) {
        return  getUsersResource(realm)
                .searchByUsername(username, exact);

    }

    public String addUser(UserDTO userDTO, String realm){


        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.getUserName());
        user.setFirstName(userDTO.getFirstname());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmailId());
        user.setEnabled(true);

        try (Response response = getUsersResource(realm).create(user)) {
            if (response.getStatus() == 201) {
                String location = response.getHeaderString("Location");
                if (location != null) {
                    return location.substring(location.lastIndexOf('/') + 1);
                }
                //TODO: Log o gestione errore
                return "";
            } else {
                //TODO: Log o gestione errore
                return "";
            }
        }
    }

    private UsersResource getUsersResource(String realm) {
        return keycloak.realm(realm)
                .users();
    }

    public void sendResetPassword(String userId, Integer lifespan, String realm){


        getUsersResource(realm).get(userId)
                .executeActionsEmail(Arrays.asList("UPDATE_PASSWORD"), lifespan);
    }
}
