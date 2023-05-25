package it.gov.pagopa.merchant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageEventDTO {
    private String id;
    private String subject;
    private String eventType;
    private StorageEventData data;
    private LocalDateTime eventTime;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StorageEventData {
        private String eTag;
        private Integer contentLength;
        private String url;
    }
}