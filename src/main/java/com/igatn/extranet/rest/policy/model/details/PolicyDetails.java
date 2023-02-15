package com.igatn.extranet.rest.policy.model.details;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

/**
 * FRE - Policy details domain model
 */

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyDetails {
    List<?> beneficiaries;
    List<?> bankAccounts;
    List<?> guarantees;
//    List<?> documents;
}
