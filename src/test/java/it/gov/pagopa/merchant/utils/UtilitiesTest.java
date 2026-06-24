package it.gov.pagopa.merchant.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @ParameterizedTest
    @CsvSource({
            "'https://www.test-imp.it', 'test-imp.it'",
            "'https://www.test.it', 'test.it'",
            "'https://test.it', test.it",
            "'http://test.it', test.it",
            "'www.test.it', test.it",
            "'https://example.com/', 'example.com'",
            "'https://  exa  mpl  e.c om /', 'example.com'",
            "'HTTPS://WWW.EXAMPLE.COM', 'example.com'"
    })
    void givenFullWebsiteWhenSanitizeDomainThenReturnSanitizedDomain(String website, String expectedResult) {
        String result = Utilities.sanitizeDomain(website);
        assertEquals(expectedResult, result);
    }

    @Test
    void givenNullWebsiteWhenSanitizeDomainThenReturnNull() {
        String result = Utilities.sanitizeDomain(null);
        assertNull(result);
    }

    @Test
    void sanitizeForLog_shouldReturnNull_whenInputIsNull() {
        assertNull(Utilities.sanitizeForLog(null));
    }

    @Test
    void sanitizeForLog_shouldReturnSameString_whenInputIsAlreadyValid() {
        String input = "merchant_123-test@example.com";

        assertEquals(input, Utilities.sanitizeForLog(input));
    }

    @Test
    void sanitizeForLog_shouldReplaceInvalidCharactersWithUnderscore() {
        String input = "merchant id: 123/#";
        String expected = "merchant_id__123__";

        assertEquals(expected, Utilities.sanitizeForLog(input));
    }

    @Test
    void sanitizeForLog_shouldRemoveUnicodeLineSeparators() {
        String input = "abc\u2028def\u2029ghi";
        String expected = "abcdefghi";

        assertEquals(expected, Utilities.sanitizeForLog(input));
    }

}
