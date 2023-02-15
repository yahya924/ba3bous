package com.igatn.extranet.rest.reimbursement.model;

import lombok.Data;

/**
 * Reimbursement model
 */
@Data
class ReimbursementDetails {
    private int id;
    private Double ssReimbursement, ourReimbursement, beneficiaryExpenses, otherInsuredReimbursement, expenses;
    private String beneficiaryFirstName, beneficiaryLastName, act, treatmentDate, status;
    private boolean hasMissingDocs;
    private MissingDocs[] missingDocs;
}
