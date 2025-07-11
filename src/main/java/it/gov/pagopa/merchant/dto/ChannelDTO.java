package it.gov.pagopa.merchant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.merchant.dto.enums.ChannelTypeEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelDTO {

    @NotBlank
    @JsonProperty("type")
    private ChannelTypeEnum type;

    @NotBlank
    @JsonProperty("contact")
    private String contact;
}
