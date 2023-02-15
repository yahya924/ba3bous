package com.igatn.extranet.rest.beneficiaries.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Beneficiaries {
    int id;
    String firstname, lastname, relation;
}
