package it.gov.pagopa.merchant.connector.pdnd.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import it.gov.pagopa.merchant.connector.pdnd.dto.JwtConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static it.gov.pagopa.merchant.connector.pdnd.constant.PdndConst.PDND_VISURA_CLIENT_ASSERTION_CACHE;

@Slf4j
@Component
public class AssertionGeneratorVisura {
    @Cacheable(value = PDND_VISURA_CLIENT_ASSERTION_CACHE, key = "#jwtCfg.kid", cacheManager = PDND_VISURA_CLIENT_ASSERTION_CACHE)
    public String generateClientAssertion(JwtConfig jwtCfg, String privateKey) {
        log.info("START - AssertionGenerator.generateClientAssertion");
        long startTime = System.currentTimeMillis();
        Instant now = Instant.now();
        Algorithm alg = Algorithm.RSA256(KeyGenerator.getPrivateKey(privateKey));
        String jwtToken = JWT.create()
                .withSubject(jwtCfg.getSubject())
                .withIssuer(jwtCfg.getIssuer())
                .withAudience(jwtCfg.getAudience())
                .withKeyId(jwtCfg.getKid())
                .withClaim("purposeId", jwtCfg.getPurposeId())
                .withExpiresAt(now.plus(Duration.ofHours(1)))
                .withJWTId(UUID.randomUUID()
                        .toString())
                .withIssuedAt(now)
                .sign(alg);

        log.info("END - AssertionGenerator.generateClientAssertion Timelapse: {} ms", System.currentTimeMillis() - startTime);
        return jwtToken;
    }
}
