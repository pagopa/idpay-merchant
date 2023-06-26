package it.gov.pagopa.merchant.utils;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilitiesTest {

    @Test
    void testPageable(){
        Pageable pageable = PageRequest.of(1,5);
        Pageable result = Utilities.getPageable(pageable);

        assertEquals(result, pageable);
    }
    @Test
    void testPageable_null(){
        Pageable result = Utilities.getPageable(null);
        Utilities.performanceLog(System.currentTimeMillis(), "");

        assertEquals(result, PageRequest.of(0, 15, Sort.by("updateDate")));
    }
}
