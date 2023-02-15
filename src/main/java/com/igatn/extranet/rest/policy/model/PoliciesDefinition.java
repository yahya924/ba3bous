package com.igatn.extranet.rest.policy.model;

import lombok.*;

import java.util.List;

/**
 * FRE - insured policies list model
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class PoliciesDefinition {
    Boolean success;
    int totalResults;
    int totalPages;
    String[] errorMsgs;
    UserPolicy[] items;
}

