package it.gov.pagopa.merchant.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.UUID;

@Slf4j
@Component
public class Utilities {

    public static final ZoneId ZONEID = ZoneId.of("Europe/Rome");
    public Pageable getPageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, 15, Sort.by("updateDate"));
        }
        return pageable;
    }
    public void performanceLog(long startTime, String service){
        log.info(
                "[PERFORMANCE_LOG] [{}] Time occurred to perform business logic: {} ms",
                service,
                System.currentTimeMillis() - startTime);
    }

    public String toUUID(String str){
        return UUID.nameUUIDFromBytes(str.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
