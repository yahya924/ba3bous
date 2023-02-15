package com.igatn.extranet.rest.exchange.service;

import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.exchange.model.WsExchangesDefinition;
import com.igatn.extranet.rest.exchange.model.WsExchangesDefinition.WsListExchangeParamsInternal;
import com.igatn.extranet.rest.exchange.model.WsExchangesDefinition.FormattedExchange;



public interface ExchangeService {

    FormattedExchange getAll(WsListExchangeParamsInternal params , User user);
}
