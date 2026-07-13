package it.gov.pagopa.merchant.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataEncryptionUtilsTest {

    private static final String VALID_KEY = "12345678901234567890123456789012";
    private static final String VALID_IV = "1234567890123456";
    private static final String PLAIN_TEXT = "SensitiveData";

    @Test
    void encryptionAndDecryptionWithValidInputs() {
        String encrypted = DataEncryptionUtils.encrypt(PLAIN_TEXT, VALID_KEY, VALID_IV);
        String decrypted = DataEncryptionUtils.decrypt(encrypted, VALID_KEY, VALID_IV);

        assertEquals(PLAIN_TEXT, decrypted);
    }

    @Test
    void encryptionFailsWithInvalidKey() {
        String invalidKey = "shortKey";

        assertThrows(RuntimeException.class, () ->
            DataEncryptionUtils.encrypt(PLAIN_TEXT, invalidKey, VALID_IV)
        );
    }



    @Test
    void decryptionFailsWithInvalidKey() {
        String encrypted = DataEncryptionUtils.encrypt(PLAIN_TEXT, VALID_KEY, VALID_IV);
        String invalidKey = "shortKey";

        assertThrows(RuntimeException.class, () ->
            DataEncryptionUtils.decrypt(encrypted, invalidKey, VALID_IV)
        );
    }

    @Test
    void decryptionFailsWithInvalidIv() {
        String encrypted = DataEncryptionUtils.encrypt(PLAIN_TEXT, VALID_KEY, VALID_IV);
        String invalidIv = "shortIv";

        assertThrows(RuntimeException.class, () ->
            DataEncryptionUtils.decrypt(encrypted, VALID_KEY, invalidIv)
        );
    }

    @Test
    void encryptionAndDecryptionWithNullInput() {
        String encrypted = DataEncryptionUtils.encrypt(null, VALID_KEY, VALID_IV);
        String decrypted = DataEncryptionUtils.decrypt(null, VALID_KEY, VALID_IV);

        assertNull(encrypted);
        assertNull(decrypted);
    }

    @Test
    void encryptionFailsWithEmptyKey() {
        String emptyKey = "";

        assertThrows(RuntimeException.class, () ->
            DataEncryptionUtils.encrypt(PLAIN_TEXT, emptyKey, VALID_IV)
        );
    }

    @Test
    void encryptionFailsWithEmptyIv() {
        String emptyIv = "";

        assertThrows(RuntimeException.class, () ->
            DataEncryptionUtils.encrypt(PLAIN_TEXT, VALID_KEY, emptyIv)
        );
    }

    @Test
    void encryptionAndDecryptionWithEmptyPlainText() {
        String emptyPlainText = "";

        String encrypted = DataEncryptionUtils.encrypt(emptyPlainText, VALID_KEY, VALID_IV);
        String decrypted = DataEncryptionUtils.decrypt(encrypted, VALID_KEY, VALID_IV);

        assertEquals(emptyPlainText, decrypted);
    }

    @Test
    void encryptionAndDecryptionWithSpecialCharacters() {
        String specialCharText = "SensitiveData!@#$%^&*()_+";

        String encrypted = DataEncryptionUtils.encrypt(specialCharText, VALID_KEY, VALID_IV);
        String decrypted = DataEncryptionUtils.decrypt(encrypted, VALID_KEY, VALID_IV);

        assertEquals(specialCharText, decrypted);
    }


    @Test
    void encryptionFailsWithTooLongKey() {
        String longKey = "123456789012345678901234567890123456";

        assertThrows(RuntimeException.class, () ->
            DataEncryptionUtils.encrypt(PLAIN_TEXT, longKey, VALID_IV)
        );
    }



    @Test
    void decryptionFailsWithCorruptedCipherText() {
        String corruptedCipherText = "InvalidCipherText";

        assertThrows(RuntimeException.class, () ->
            DataEncryptionUtils.decrypt(corruptedCipherText, VALID_KEY, VALID_IV)
        );
    }
}