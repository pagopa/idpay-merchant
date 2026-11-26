package it.gov.pagopa.merchant.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.net.IDN;
import java.net.URI;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

@Slf4j
public final class Utilities {
    private Utilities() {}

    public static Pageable getPageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, 15, Sort.by("updateDate"));
        }
        return pageable;
    }

    public static void performanceLog(long startTime, String service){
        log.info(
                "[PERFORMANCE_LOG] [{}] Time occurred to perform business logic: {} ms",
                service,
                System.currentTimeMillis() - startTime);
    }


    public static String toUUID(String str){
        return UUID.nameUUIDFromBytes(str.getBytes(StandardCharsets.UTF_8)).toString();
    }

    public static String sanitizeString(String str){
        return str.replaceAll("[\\r\\n]", "").replaceAll("[^\\w\\s-]", "");
    }

    public static String normalizeWebsiteHost(String url){
        if (StringUtils.isBlank(url)) {
            return null;
        }
        try {
            URI uri = new URI(url.trim());
            String host = uri.getHost();

            if (host == null) {
                throw new IllegalArgumentException("Website URL does not contain a valid host");
            }

            host = IDN.toASCII(host).toLowerCase(Locale.ROOT);

            if (host.endsWith(".")) {
                host = host.substring(0, host.length() - 1);
            }

            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            return host;
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Website URL does not contain a valid host", exception);
        }

    }

    public static String normalizeFranchiseName(String franchiseName) {
        return StringUtils.isNotBlank(franchiseName)
                ? franchiseName.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT)
                : franchiseName;
    }

    public static String spaceRemover(String name) {
        return StringUtils.isNotBlank(name)
                ? name.trim().replaceAll("\\s+", " ")
                : name;
    }

    public static String sanitizeForLog(String input) {
        if (input == null) {
            return null;
        }

        String sanitized = input.replaceAll("[\\p{Cntrl}\\u2028\\u2029]", "");
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9@._-]", "_");
        return sanitized.trim();
    }

}
