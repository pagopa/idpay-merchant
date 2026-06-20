package it.gov.pagopa.merchant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitiativeSearchRequest {

    @NotNull
    private Set<String> onboardedIds;

    @NotNull
    private List<String> atecoCodes;
}