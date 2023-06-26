package it.gov.pagopa.merchant.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.UUID;

@Slf4j
public final class Utilities {
    private Utilities() {}

    public static final ZoneId ZONEID = ZoneId.of("Europe/Rome");

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
}
