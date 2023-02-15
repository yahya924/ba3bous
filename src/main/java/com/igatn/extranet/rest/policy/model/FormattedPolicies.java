package com.igatn.extranet.rest.policy.model;

import lombok.*;


@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class FormattedPolicies {
    Boolean success;
    int totalResults;
    UserPolicy[] policies;
}
