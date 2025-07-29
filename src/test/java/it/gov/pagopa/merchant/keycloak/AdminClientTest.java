package it.gov.pagopa.merchant.keycloak;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(
        classes = {
                AdminClient.class,
                AdminClientService.class
        })
class AdminClientTest {

    @Autowired
    private Keycloak keycloak;

    @Autowired
    private AdminClientService adminClientService;

    @Test
    void testKeycloakConnection() {
        // Prova a recuperare le informazioni sul realm per verificare la connessione
        var realmResource = keycloak.realm("merchant-operator");
        assertNotNull(realmResource);
        var realmRepresentation = realmResource.toRepresentation();
        assertEquals("merchant-operator", realmRepresentation.getRealm());
    }

    @Test
    void searchUserTest(){

        Assert.notEmpty(adminClientService.searchByUsername("test", true, "merchant-operator"), "error in search user");

    }

    @Test
    void createUser(){

        UserDTO user = new UserDTO();
        user.setUserName("test3");
        user.setFirstname("firstName");
        user.setLastName("lastName");
        user.setEmailId("test@test.it");

        String resultAdd = adminClientService.addUser(user, "merchant-operator");

        Assert.hasText(resultAdd, "error in add user");

    }

    @Test
    void sendResetPassword(){
        final String userId = "USER_ID";

        adminClientService.sendResetPassword(userId, 120, "merchant-operator");

    }

}
