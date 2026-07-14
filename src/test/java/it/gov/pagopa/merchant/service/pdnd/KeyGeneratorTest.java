package it.gov.pagopa.merchant.service.pdnd;

import it.gov.pagopa.merchant.exception.custom.InternalException;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KeyGeneratorTest {

    @Test
    void getPrivateKey_validKey_returnsPrivateKey() throws Exception {

        KeyPairGenerator generator =
                KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        KeyPair keyPair = generator.generateKeyPair();

        String privateKey =
                "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getMimeEncoder(64, "\n".getBytes())
                        .encodeToString(
                                keyPair.getPrivate().getEncoded()
                        ) +
                "\n-----END PRIVATE KEY-----";

        RSAPrivateKey result =
                KeyGenerator.getPrivateKey(privateKey);

        assertNotNull(result);
    }

    @Test
    void getPrivateKey_emptyKey_throwsInternalException() {

        assertThrows(
                InternalException.class,
                () -> KeyGenerator.getPrivateKey("")
        );
    }

    @Test
    void getPrivateKey_nullKey_throwsNullPointerException() {

        assertThrows(
                NullPointerException.class,
                () -> KeyGenerator.getPrivateKey(null)
        );
    }

    @Test
    void getPublicKey_validKey_returnsPublicKey() throws Exception {

        KeyPairGenerator generator =
                KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        KeyPair keyPair = generator.generateKeyPair();

        String publicKey =
                "-----BEGINPUBLICKEY-----" +
                Base64.getEncoder()
                        .encodeToString(
                                keyPair.getPublic().getEncoded()
                        ) +
                "-----ENDPUBLICKEY-----";

        RSAPublicKey result =
                KeyGenerator.getPublicKey(publicKey);

        assertNotNull(result);
    }

    @Test
    void getPublicKey_invalidKey_throwsInvalidKeySpecException() {

        assertThrows(
                Exception.class,
                () -> KeyGenerator.getPublicKey("invalid-key")
        );
    }

    @Test
    void getPublicKey_emptyKey_throwsInvalidKeySpecException() {

        assertThrows(
                Exception.class,
                () -> KeyGenerator.getPublicKey("")
        );
    }

    @Test
    void getPublicKey_nullKey_throwsNullPointerException() {

        assertThrows(
                NullPointerException.class,
                () -> KeyGenerator.getPublicKey(null)
        );
    }
}