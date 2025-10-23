package it.gov.pagopa.merchant.constants;

import java.util.Set;

public class AggregationConstants {

  private AggregationConstants() {
  }

  public static final String LOWER_SUFFIX = "_lower";

  public static final String CONTACT_SURNAME_LOWER_SUFFIX = "contactSurname_lower";

  public static final Set<String> CASE_INSENSITIVE_FIELDS = Set.of(
      "contactName", "contactEmail", "website", "franchiseName"
  );

}
