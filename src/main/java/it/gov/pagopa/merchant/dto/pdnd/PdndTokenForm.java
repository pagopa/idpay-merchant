package it.gov.pagopa.merchant.dto.pdnd;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PdndTokenForm {
    @feign.form.FormProperty("client_assertion") String clientAssertion;
    @feign.form.FormProperty("client_assertion_type") String clientAssertionType;
    @feign.form.FormProperty("grant_type") String grantType;
    @feign.form.FormProperty("client_id") String clientId;
}
