package com.igatn.extranet.rest.policy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * FRE - insured policy domain model
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPolicy {
  String id, risk, startDate, endDate, currency, numContrat;
  int sumBeneficiaries;
}
