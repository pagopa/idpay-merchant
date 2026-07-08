package it.gov.pagopa.merchant.service.pdnd.assertion;

import it.gov.pagopa.merchant.dto.pdnd.JwtConfig;
import it.gov.pagopa.merchant.service.pdnd.KeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class AssertionGeneratorTest {

    private AssertionGenerator assertionGenerator;

    @BeforeEach
    void setUp() {
        assertionGenerator = new AssertionGenerator();
    }

    @Test
    void generateClientAssertion_success() {
        JwtConfig jwtCfg = JwtConfig.builder()
                .subject("subject")
                .issuer("issuer")
                .audience("audience")
                .kid("kid")
                .purposeId("purposeId")
                .build();
        String privateKey = "privateKey";

        try (MockedStatic<KeyGenerator> mockedKeyGenerator = mockStatic(KeyGenerator.class)) {
            mockedKeyGenerator.when(() -> KeyGenerator.getPrivateKey(privateKey))
                    .thenReturn(mock(java.security.interfaces.RSAPrivateKey.class));

            String jwtToken = assertionGenerator.generateClientAssertion(jwtCfg, privateKey);

            assertNotNull(jwtToken);
        }
    }
}