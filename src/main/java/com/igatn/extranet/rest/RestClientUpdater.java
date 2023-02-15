package com.igatn.extranet.rest;

import com.igatn.extranet.domainjpa.impl.domain.tracking.AppAuthHistory;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.service.history.AuthHistoryService;
import io.micrometer.core.lang.NonNullApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * FRE - add customization to rest template bean 
 */
@Slf4j
@NonNullApi
@Component
public class RestClientUpdater implements ClientHttpRequestInterceptor {
    
    private final AuthHistoryService authHistoryService;
    
    RestClientUpdater(AuthHistoryService authHistoryService) {
        this.authHistoryService = authHistoryService;
    }

    /**
     * FRE - Intercept RestTemplate calls;
     * 
     * @param request
     * @param body
     * @param execution
     * @return
     * @throws IOException
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        // Check "authorization" header if it exists explicitly in request
        Optional<List<String>> authorizations = Optional
            .of(request)
            .map(HttpRequest::getHeaders)
            .map(h -> h.get(HttpHeaders.AUTHORIZATION));

        // if no explicit authorization header
        // then use the one stored in db dedicated for the current user
        if(authorizations.isEmpty()) {
            
            // get user (not null)
            Optional<Object> principal = Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal);

            if (principal.isPresent()) {

                User authenticatedUser = (User) principal.get();
                
                AppAuthHistory authHistory = authHistoryService.getByToken(authenticatedUser.getAuthToken());
               
                authHistoryService.validate(authHistory);
                
                String externalAuthToken = authHistory
                    .getExternalHistory()
                    .getAuthToken();
                
                if(!StringUtils.hasText(externalAuthToken))
                    throw new SecurityException(
                        "The external token for user with ID : " + authenticatedUser.getId() + " is invalid!"
                    );
                    
                request.getHeaders().setBearerAuth(externalAuthToken);
                
            }
        }

        return execution.execute(request, body);
    }
}
