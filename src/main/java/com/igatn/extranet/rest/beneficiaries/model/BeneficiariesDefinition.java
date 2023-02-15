package com.igatn.extranet.rest.beneficiaries.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class BeneficiariesDefinition {
    Boolean success;
    String[] errorMsgs;
    String[] infoMsgs;
    Beneficiaries[] item;
}
