package com.igatn.extranet.rest.reimbursement.model;

import lombok.Data;

/**
 * Reimbursement list definition model
 */
@Data
public
class ReimbursementDetailsListDefinition {
    private Boolean success;
    private int totalResults;
    private int totalPages;
    private String[] errorMsgs;
    private ReimbursementDetails[] items;
}
