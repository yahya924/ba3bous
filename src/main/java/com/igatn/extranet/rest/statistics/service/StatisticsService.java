package com.igatn.extranet.rest.statistics.service;

import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.statistics.model.WsStatisticsDefinition.WsListStatisticsResponseInternal;
import com.igatn.extranet.rest.statistics.model.WsStatisticsDefinition.WsListStatisticsParamsInternal;

public interface StatisticsService {

    WsListStatisticsResponseInternal  getAll(WsListStatisticsParamsInternal params, User user);

}
