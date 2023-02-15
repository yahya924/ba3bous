package com.igatn.extranet.rest.beneficiaries.service;

import com.igatn.extranet.rest.beneficiaries.model.FormattedBeneficiaries;
import org.h2.engine.User;

public interface BeneficiariesService {
    FormattedBeneficiaries getAll(User user);
}
