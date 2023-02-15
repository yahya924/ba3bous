package com.igatn.extranet.service.history;

import com.igatn.extranet.app.AppProperties;
import com.igatn.extranet.app.AppProperties.Security;
import com.igatn.extranet.app.AppProperties.Security.IGASecurityExpirations;
import com.igatn.extranet.app.AppProperties.Security.IGASecurityKeys;
import com.igatn.extranet.app.AppService;
import com.igatn.extranet.domainjpa.api.data.auth.AppAuthHistoryRepository;
import com.igatn.extranet.domainjpa.api.data.auth.ExternalAuthHistoryRepository;
import com.igatn.extranet.domainjpa.impl.domain.tracking.AppAuthHistory;
import com.igatn.extranet.domainjpa.impl.domain.tracking.ExternalAuthHistory;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.domainjpa.utils.DateTimeInstantUtils;
import com.igatn.extranet.rest.models.ws.WsExternalResponse;
import com.igatn.extranet.rest.models.ws.WsSignInExternalResponse;
import com.igatn.extranet.rest.user.models.BasicCredentials;
import com.igatn.extranet.utils.ExtranetUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Slf4j
@Service
public class AuthHistoryServiceImpl implements AuthHistoryService {
    
    private final AppAuthHistoryRepository historyRepository;
    
    private final AppService appService;

    public AuthHistoryServiceImpl(
        ExternalAuthHistoryRepository externalHistoryRepository,
        AppAuthHistoryRepository appHistoryRepository,
        AppService appService
    ) {
        this.historyRepository = appHistoryRepository;
        this.appService = appService;
    }

    /**
     * Update auth history for user
     * @param credentials
     * @param user
     * @param wsResponse
     * @return
     */
    @Override
    public AppAuthHistory updateAuthHistory(
        BasicCredentials credentials, 
        User user, 
        WsExternalResponse wsResponse
    ) {

        log.info(" -- Setting auth history...");

        /*************** Auth history *****************/

        if(!(wsResponse instanceof WsSignInExternalResponse signInResponse))
            throw new SecurityException(
                "-- auth history: Invalid sign-in 'WsResponse' object type: " + wsResponse
            );
        
        ExternalAuthHistory externalHistory = new ExternalAuthHistory();
        externalHistory.setAuthToken(signInResponse.getExternalAuthToken());
        externalHistory.setUsername(credentials.username());
        externalHistory.setPassword(credentials.password());

        String signingKey = Optional
            .ofNullable(appService.getAppProperties())
            .map(AppProperties::getSecurity)
            .map(Security::getKeys)
            .map(IGASecurityKeys::getSigning)
            .orElseThrow(
                () -> new SecurityException("No 'signing' key found in properties")
            );

        int expiration = Optional
            .ofNullable(appService.getAppProperties())
            .map(AppProperties::getSecurity)
            .map(Security::getExpiration)
            .map(IGASecurityExpirations::getJwtToken)
            .orElseThrow(
                () -> new SecurityException("No 'signing' key-expiration found in properties")
            );
        
        String secureToken = ExtranetUtils.buildApiSecurityToken(credentials.username(), signingKey, expiration);
        
        AppAuthHistory history = new AppAuthHistory();
        history.setExpiresAt(
            DateTimeInstantUtils.getInstantNow().plusSeconds(expiration)
        );
        history.setToken(secureToken);
        history.setExternalHistory(externalHistory);
        history.setUser(user);
        
        historyRepository.save(history);
        
        
        log.info("New authentication history has been added for user with ID: " + user.getId());
        
        return history;
    }

    @Override
    @NotNull
    public AppAuthHistory getByToken(String token){
        
        AppAuthHistory history = historyRepository.findByToken(token)
            .orElseThrow(
                () -> new SecurityException("No auth history with token : " + token + "!")
            );
        
        return history;
    }
    
    @Override
    public void validate(@NotNull AppAuthHistory history){
        if (
            history
                .getExpiresAt()
                .isBefore(DateTimeInstantUtils.getInstantNow())
        ) {
            throw new SecurityException(
                "auth-history service: Auth token has expired for user with ID: " + history.getUser().getId()
            );
        }
    }
    
}
