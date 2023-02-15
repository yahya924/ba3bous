package com.igatn.extranet.rest.user.models;

import lombok.Data;

@Data
public class UserDetails {
    String role;
    String email;
    String firstName;
    String lastName;
    String nationality;
    String postalAddress;
    String logo;
    String noSS;
    String clientNumber;
    Long id;
    String birthDate;
    Phones phones;
    Boolean hasIndividualPolicies;
    MissingDocsDefinition hasMissingDocs;
    Boolean canPayByCard;

    @Data
    public static class MissingDocsDefinition {
        boolean policies;
        boolean reimbursements;
    }
}
