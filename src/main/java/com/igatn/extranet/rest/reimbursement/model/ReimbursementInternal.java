package com.igatn.extranet.rest.reimbursement.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

public class ReimbursementInternal {

    @Data
    public static class ReimbursementsListDefinitionInternal {
        private Boolean success;
        private int totalResults;
        private List<ReimbursementsListItemInternal> reimbursements;
    }

    @Data
    @Builder
    public static class ReimbursementsListParamsInternal {
        private String language;
        private String status;
        private int indexFrom;
        private int indexTo;
        private Double amountFrom;
        private Double amountTo;
        private String dateFrom;
        private String dateTo;
    }

    @Data
    public static class ReimbursementsListItemInternal {
        private long id;
        private Double amount, amountPaid;
        private String titular/*, paymentDate*/, creationDate, status;
        private Boolean hasMissingDocs;
    }
    
}
