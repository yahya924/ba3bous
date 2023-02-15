package com.igatn.extranet.service.history;

import com.igatn.extranet.domainjpa.impl.domain.tracking.AppAuthHistory;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.models.ws.WsExternalResponse;
import com.igatn.extranet.rest.user.models.BasicCredentials;

import javax.validation.constraints.NotNull;

public interface AuthHistoryService {
    AppAuthHistory updateAuthHistory(BasicCredentials credentials, User user, WsExternalResponse signInResponse);
    @NotNull AppAuthHistory getByToken(String token);
    void validate(@NotNull AppAuthHistory history);
}
