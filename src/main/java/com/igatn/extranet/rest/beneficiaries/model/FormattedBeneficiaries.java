package com.igatn.extranet.rest.beneficiaries.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class FormattedBeneficiaries {
    Boolean success;
    Beneficiaries[] beneficiaries;

}
