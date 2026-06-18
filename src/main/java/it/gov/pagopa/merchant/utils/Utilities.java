package it.gov.pagopa.merchant.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.nio.charset.StandardCharsets;
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

    public static String sanitizeDomain(String url){
        return StringUtils.isNotBlank(url) ? StringUtils.deleteWhitespace(url)
                .toLowerCase()
                .replace("https://", "")
                .replace("http://", "")
                .replace("www.", "")
                .split("/")[0] : null;
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
