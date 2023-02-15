package com.igatn.extranet.rest.reimbursement.service;

import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.reimbursement.model.ReimbursementInternal.ReimbursementsListDefinitionInternal;
import com.igatn.extranet.rest.reimbursement.model.ReimbursementInternal.ReimbursementsListParamsInternal;

public interface ReimbursementService {

    ReimbursementsListDefinitionInternal getAll(ReimbursementsListParamsInternal wsParams, User user);
}
