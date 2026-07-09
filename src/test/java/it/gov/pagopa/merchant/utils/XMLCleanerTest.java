package it.gov.pagopa.merchant.utils;
import it.gov.pagopa.merchant.exception.custom.XmlProcessingException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XMLCleanerTest {

    @Test
    void cleanXmlRemovesSpecifiedNodes() {
        String rawXml = "<root><removeMe>content</removeMe><keepMe>content</keepMe></root>";
        List<String> nodesToRemove = Collections.singletonList("removeMe");

        byte[] cleanedXml = XMLCleaner.cleanXml(rawXml.getBytes(StandardCharsets.UTF_8), nodesToRemove);

        String result = new String(cleanedXml, StandardCharsets.UTF_8);
        assertFalse(result.contains("<removeMe>"));
        assertTrue(result.contains("<keepMe>"));
    }

    @Test
    void cleanXmlHandlesEmptyNodeList() {
        String rawXml = "<root><keepMe>content</keepMe></root>";
        List<String> nodesToRemove = Collections.emptyList();

        byte[] cleanedXml = XMLCleaner.cleanXml(rawXml.getBytes(StandardCharsets.UTF_8), nodesToRemove);

        String result = new String(cleanedXml, StandardCharsets.UTF_8);
        assertTrue(result.contains("<keepMe>"));
    }

    @Test
    void cleanXmlThrowsExceptionForInvalidXml() {
        String invalidXml = "<root><unclosedTag>";
        List<String> nodesToRemove = Collections.singletonList("unclosedTag");

        assertThrows(XmlProcessingException.class, () -> 
            XMLCleaner.cleanXml(invalidXml.getBytes(StandardCharsets.UTF_8), nodesToRemove)
        );
    }


}