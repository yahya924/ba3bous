package com.igatn.extranet.service.externalws;

import com.igatn.extranet.domainjpa.api.data.ws.WsConfigRepository;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.ExternalWsApi;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.WsConfig;
import com.igatn.extranet.rest.exceptions.ws.ExternalWsNoResponseException;
import com.igatn.extranet.rest.models.ws.WsSignInExternalResponse;
import com.igatn.extranet.rest.user.models.BasicCredentials;
import com.igatn.extranet.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.HashMap;
import java.util.Optional;

/**
 * FRE - External SignIn service
 */
@Slf4j
@Service
public class ExternalSignInServiceImpl implements ExternalWsService {
    
    private final RestUtils restUtils;

    private final WsConfigRepository wsConfigRepository;
    
    public ExternalSignInServiceImpl(RestUtils restUtils, WsConfigRepository wsConfigRepository) {
        this.restUtils = restUtils;
        this.wsConfigRepository = wsConfigRepository;
    }
    
    @Override
    public void validateWsSignInExternalResponse(WsSignInExternalResponse signInResponse) {
        log.info(" Validating external WS response...");

        boolean responseSuccess = signInResponse.isSuccess();
        boolean responseHasData = signInResponse.hasData();
        boolean invalidWsResponse = !responseSuccess || !responseHasData;

        // in case of ws error
        if (invalidWsResponse) {

            final String DEFAULT_ERROR_MSG = "Invalid extenral response and no errors !";

            String[] externalErrorMsg = Optional.of(signInResponse)
                .map(WsSignInExternalResponse::getErrorMsgs)
                .orElseThrow(
                    () -> new ExternalWsNoResponseException(DEFAULT_ERROR_MSG)
                );

            String errorMessage = DEFAULT_ERROR_MSG;

            if (externalErrorMsg.length > 0) {

                StringBuilder errorsBuilder = new StringBuilder();

                // Build the error msg
                for (String errorMsg : externalErrorMsg) {
                    errorsBuilder.append("- ");
                    errorsBuilder.append(errorMsg);
                    errorsBuilder.append(".\n");
                }

                // override errorMessage
                errorMessage = errorsBuilder.toString();

            }

            throw new ExternalWsNoResponseException(errorMessage);
        }

        log.info("External WS response valid.");
    }

    @NotNull
    @Override
    public WsSignInExternalResponse getWsSignInExternalResponse(BasicCredentials credentials, ExternalWsApi externalWsApi, WsConfig wsSignInConfig) {

        log.info(" Launching external WS...");

        // build normalized URI
        URI wsUri = UriComponentsBuilder
            .fromUri(externalWsApi.getAsUri())
            .path(wsSignInConfig.getPath())
            .build()
            .toUri();

        // It's better to use 'external' keyword rather than company name(s)
        ResponseEntity<WsSignInExternalResponse> signInExternalResponse = restUtils.preparePostWS(
            wsUri.toString(),
            new HashMap<>(),
            WsSignInExternalResponse.class,
            credentials
        );

        // external ws response body
        WsSignInExternalResponse signInResponse = Optional.ofNullable(signInExternalResponse)
            .map(ResponseEntity::getBody)
            .orElseThrow(
                () -> new ExternalWsNoResponseException("No response from external service POST/signIn")
            );

        log.info("External WS has been launched.");
        return signInResponse;
    }

    @NotNull
    @Override
    public WsConfig getExternalWsConfig(ExternalWsApi externalWsApi) {

        log.info("Validating external WSConfig...");

        // FRE - 
        // FIXME: its better to use an id rather than label here
        //  for example: "ws-sign-in"
        final String WS_SIGN_IN_NAME = "Insured sign in";

        WsConfig wsSignInConfig = wsConfigRepository.findByHostAndLabel(
                externalWsApi,
                WS_SIGN_IN_NAME
            )
            .orElseThrow(
                () -> new IllegalArgumentException("No external WS config with label '" + WS_SIGN_IN_NAME)
            );

        boolean isValidHttpMethod = wsSignInConfig.getMethod().equals(HttpMethod.POST);

        if (!isValidHttpMethod)
            throw new IllegalArgumentException(
                "Invalid HTTP method for WS with ID: " + wsSignInConfig.getId()
            );

        log.info("WsConfig is valid.");

        return wsSignInConfig;
    }
    
}
