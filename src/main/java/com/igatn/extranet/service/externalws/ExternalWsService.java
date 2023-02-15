package com.igatn.extranet.service.externalws;

import com.igatn.extranet.domainjpa.impl.domain.ws.entities.ExternalWsApi;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.WsConfig;
import com.igatn.extranet.rest.models.ws.WsSignInExternalResponse;
import com.igatn.extranet.rest.user.models.BasicCredentials;

import javax.validation.constraints.NotNull;

public interface ExternalWsService {

    @NotNull WsSignInExternalResponse getWsSignInExternalResponse(BasicCredentials credentials, ExternalWsApi externalWsApi, WsConfig wsSignInConfig);

    @NotNull WsConfig  getExternalWsConfig(ExternalWsApi externalWsApi);
    
    void validateWsSignInExternalResponse(WsSignInExternalResponse signInResponse);
    
}
