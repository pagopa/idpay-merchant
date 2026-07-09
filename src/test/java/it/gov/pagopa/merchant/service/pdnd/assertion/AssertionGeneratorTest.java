package it.gov.pagopa.merchant.service.pdnd.assertion;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import it.gov.pagopa.merchant.dto.pdnd.JwtConfig;
import it.gov.pagopa.merchant.service.pdnd.KeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class AssertionGeneratorTest {

    private AssertionGenerator assertionGenerator;

    @BeforeEach
    void setUp() {
        assertionGenerator = new AssertionGenerator();
    }

    @Test
    void generateClientAssertion_validInput_returnsJwtToken() throws Exception {

        JwtConfig jwtCfg = JwtConfig.builder()
                .subject("subject")
                .issuer("issuer")
                .audience("audience")
                .kid("kid")
                .purposeId("purposeId")
                .build();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        try (MockedStatic<KeyGenerator> mockedStatic =
                     mockStatic(KeyGenerator.class)) {

            mockedStatic.when(() -> KeyGenerator.getPrivateKey("privateKey"))
                    .thenReturn(privateKey);

            String token =
                    assertionGenerator.generateClientAssertion(
                            jwtCfg,
                            "privateKey"
                    );

            assertNotNull(token);

            DecodedJWT jwt = JWT.decode(token);

            assertEquals("subject", jwt.getSubject());
            assertEquals("issuer", jwt.getIssuer());
            assertEquals("kid", jwt.getKeyId());

            assertEquals(
                    "audience",
                    jwt.getAudience().get(0)
            );

            assertEquals(
                    "purposeId",
                    jwt.getClaim("purposeId").asString()
            );

            Map<String, Object> digest =
                    jwt.getClaim("digest").asMap();

            assertNotNull(digest);
            assertEquals("SHA-256", digest.get("alg"));
            assertEquals("abc123", digest.get("value"));

            assertNotNull(jwt.getIssuedAt());
            assertNotNull(jwt.getExpiresAt());
            assertNotNull(jwt.getId());
        }
    }

    @Test
    void generateClientAssertion_keyGeneratorThrowsException_propagatesException() {

        JwtConfig jwtCfg = JwtConfig.builder()
                .subject("subject")
                .issuer("issuer")
                .audience("audience")
                .kid("kid")
                .purposeId("purposeId")
                .build();

        try (MockedStatic<KeyGenerator> mockedStatic =
                     mockStatic(KeyGenerator.class)) {

            mockedStatic.when(() -> KeyGenerator.getPrivateKey("invalid"))
                    .thenThrow(
                            new IllegalArgumentException("Invalid private key")
                    );

            IllegalArgumentException exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> assertionGenerator.generateClientAssertion(
                                    jwtCfg,
                                    "invalid"
                            )
                    );

            assertEquals(
                    "Invalid private key",
                    exception.getMessage()
            );
        }
    }
}