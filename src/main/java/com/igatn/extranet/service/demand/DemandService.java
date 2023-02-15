package com.igatn.extranet.service.demand;

import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.user.models.MissingDocsDefinition.WsMissingDocsResponse;
import com.igatn.extranet.rest.user.models.MissingDocsListParams;

public interface DemandService {
    
    WsMissingDocsResponse getAll(MissingDocsListParams queryParams, User user);
}
