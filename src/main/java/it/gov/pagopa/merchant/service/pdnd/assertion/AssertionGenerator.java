package it.gov.pagopa.merchant.service.pdnd.assertion;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import it.gov.pagopa.merchant.dto.pdnd.JwtConfig;
import it.gov.pagopa.merchant.service.pdnd.KeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static it.gov.pagopa.merchant.constants.PdndConst.PDND_CLIENT_ASSERTION_CACHE;
import static it.gov.pagopa.merchant.constants.PdndConst.REDIS_CACHE_MANAGER;


@Slf4j
@Component
public class AssertionGenerator {
    @Cacheable(value = PDND_CLIENT_ASSERTION_CACHE, key = "#jwtCfg.kid", cacheManager = REDIS_CACHE_MANAGER)
    public String generateClientAssertion(JwtConfig jwtCfg, String privateKey) {
        log.info("[PDND-ASSERTION] generateClientAssertion START");
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

        log.info("[PDND-ASSERTION] generateClientAssertion END - elapsed [{}] ms", System.currentTimeMillis() - startTime);
        return jwtToken;
    }
}
