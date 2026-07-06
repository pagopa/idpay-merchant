package it.gov.pagopa.merchant.dto.initiative;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import it.gov.pagopa.initiative.dto.rule.refund.InitiativeRefundRuleDTO;
import it.gov.pagopa.initiative.dto.rule.reward.InitiativeRewardRuleDTO;
import it.gov.pagopa.initiative.dto.rule.trx.InitiativeTrxConditionsDTO;
import it.gov.pagopa.initiative.utils.validator.ValidationApiEnabledGroup;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

/**
 * InitiativeDTO
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InitiativeDTO {

  /**
   * Gets or Sets initiativeRewardType
   */
  public enum InitiativeRewardTypeEnum {
    REFUND("REFUND"), DISCOUNT("DISCOUNT");

    private final String value;

    InitiativeRewardTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static InitiativeRewardTypeEnum fromValue(String text) {
      for (InitiativeRewardTypeEnum b : InitiativeRewardTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("initiativeId")
  private String initiativeId;

  @JsonProperty("initiativeName")
  private String initiativeName;

  @JsonProperty("organizationId")
  private String organizationId;
  @JsonProperty("organizationName")
  private String organizationName;

  @JsonProperty("status")
  private String status;

  @JsonProperty("creationDate")
  private LocalDateTime creationDate;

  @JsonProperty("updateDate")
  private LocalDateTime updateDate;

  @JsonProperty("autocertificationCheck")
  private Boolean autocertificationCheck;

  @JsonProperty("beneficiaryRanking")
  private Boolean beneficiaryRanking;

  @JsonProperty("general")
  private InitiativeGeneralDTO general;

  @JsonProperty("additionalInfo")
  private InitiativeAdditionalDTO additionalInfo;

  @JsonProperty("beneficiaryRule")
  private InitiativeBeneficiaryRuleDTO beneficiaryRule;

  @JsonProperty("initiativeRewardType")
  @NotNull(groups = ValidationApiEnabledGroup.class)
  private InitiativeDTO.InitiativeRewardTypeEnum initiativeRewardType;

  @JsonProperty("rewardRule")
  private InitiativeRewardRuleDTO rewardRule;

  @JsonProperty("trxRule")
  private InitiativeTrxConditionsDTO trxRule;

  @JsonProperty("refundRule")
  private InitiativeRefundRuleDTO refundRule;
  @JsonProperty("isLogoPresent")
  private Boolean isLogoPresent;

  @JsonProperty("atecoCodes")
  private List<String> atecoCodes;
}
