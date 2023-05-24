package it.gov.pagopa.merchant;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.net.UnknownHostException;

@SpringBootTest
@TestPropertySource(
        properties = {
                // even if enabled into application.yml, spring test will not load it
                // https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing.spring-boot-applications.jmx
                "spring.jmx.enabled=true",
                // region mongodb
                "logging.level.org.mongodb.driver=WARN",
                "logging.level.org.springframework.boot.autoconfigure.mongo.embedded=WARN",
                "de.flapdoodle.mongodb.embedded.version=4.0.21",
                // endregion
        })
public abstract class BaseIntegrationTest {

    @Autowired
    private MongoProperties mongoProperties;

    @Value("${spring.data.mongodb.uri}")
    private String mongodbUri;

    @PostConstruct
    public void logEmbeddedServerConfig() throws NoSuchFieldException, UnknownHostException {
        String mongoUrl = mongoProperties.getUri().replaceFirst("(?<=//)[^@]+@", "");

        System.out.printf("""
                        ************************
                        Embedded mongo: %s
                        ************************
                        """,
                mongoUrl);
    }

}
