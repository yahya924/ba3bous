package com.igatn.extranet.rest.premiums.service;

import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.premiums.model.WsPremiumDefinition.WsListPremiumsParamsInternal;
import com.igatn.extranet.rest.premiums.model.WsPremiumDefinition.WsListPremiumsResponseInternal;

public interface PremiumService {

    WsListPremiumsResponseInternal getAll(WsListPremiumsParamsInternal params, User user);

}
