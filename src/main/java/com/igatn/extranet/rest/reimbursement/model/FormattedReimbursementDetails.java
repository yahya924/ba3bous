package com.igatn.extranet.rest.reimbursement.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public
class FormattedReimbursementDetails {
    private Boolean success;
    private int totalResults;
    private ReimbursementDetails[] details;
}
