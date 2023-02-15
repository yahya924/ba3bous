package com.igatn.extranet.rest.reimbursement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.igatn.extranet.domainjpa.api.data.ws.ExternalWsApiRepository;
import com.igatn.extranet.domainjpa.api.data.ws.WsConfigRepository;
import com.igatn.extranet.domainjpa.impl.domain.client.Client;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.ExternalWsApi;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.WsConfig;
import com.igatn.extranet.domainjpa.impl.domain.ws.model.WsReimbursementExternal;
import com.igatn.extranet.domainjpa.impl.domain.ws.model.WsReimbursementExternal.WsListReimbursementsExternal;
import com.igatn.extranet.rest.exceptions.ClientNotFoundException;
import com.igatn.extranet.rest.exceptions.OccuredErrorException;
import com.igatn.extranet.rest.exceptions.ws.ExternalWsNoResponseException;
import com.igatn.extranet.rest.reimbursement.model.ReimbursementInternal.ReimbursementsListDefinitionInternal;
import com.igatn.extranet.rest.reimbursement.model.ReimbursementInternal.ReimbursementsListItemInternal;
import com.igatn.extranet.rest.reimbursement.model.ReimbursementInternal.ReimbursementsListParamsInternal;
import com.igatn.extranet.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReimbursementServiceImpl implements ReimbursementService {

    @Autowired
    private RestUtils restUtils;

    @Autowired
    private ExternalWsApiRepository externalWsApiRepository;

    @Autowired
    private WsConfigRepository wsConfigRepository;
    
    @Override
    public ReimbursementsListDefinitionInternal getAll(ReimbursementsListParamsInternal params, User user) {
        
        String exceptionMsg = "";
        final String WS_LIST_REIMBURSEMENTS_LABEL = "reimbursements list";
        
        try {
            
            Client client = user.getClient();
            log.info("Searching unique WS external API by client: " + client.getName());

            ExternalWsApi externalWsApi = externalWsApiRepository.findById(client.getId()).
                    orElseThrow(() -> new ClientNotFoundException("client not found!"));

            log.info("Found WS external API: " + externalWsApi.getLabel() + "!");
            
            // get ws
            String wsReimbursementsListPath =  "/"+ 
                    WsReimbursementExternal.BASE_PATH + 
                    "/" + WsReimbursementExternal.LIST_PATH;

            log.info("Searching unique Config for WS " + WS_LIST_REIMBURSEMENTS_LABEL+"...");

            Optional<WsConfig> wsReimbursementsListExternalConfig = wsConfigRepository
                    .findByHostIdAndPath(externalWsApi.getId(),wsReimbursementsListPath);

            log.info("WS Config for WS " + WS_LIST_REIMBURSEMENTS_LABEL+" found!");

            if(wsReimbursementsListExternalConfig.isPresent()){

                WsConfig wsReimbursementsListConfig = wsReimbursementsListExternalConfig.get();

                log.info("Building full path to call external WS " + WS_LIST_REIMBURSEMENTS_LABEL);

                StringBuilder wsFullPathBuilder = new StringBuilder();
                wsFullPathBuilder.append(externalWsApi.getRootPath());
                wsFullPathBuilder.append(wsReimbursementsListConfig.getPath());
                String wsFullPath = wsFullPathBuilder.toString();

                log.info("Building URI to call WS " + WS_LIST_REIMBURSEMENTS_LABEL);

                URI wsUri = UriComponentsBuilder
                        .fromUri(externalWsApi.getAsUri())
                        .replacePath(wsFullPath)
                        .queryParam(WsReimbursementExternal.PARAM_LANGUAGE, params.getLanguage())
                        .queryParamIfPresent(WsReimbursementExternal.PARAM_STATUS, Optional.ofNullable(params.getStatus()))
                        .queryParam(WsReimbursementExternal.PARAM_RESULT_FROM, params.getIndexFrom())
                        .queryParam(WsReimbursementExternal.PARAM_RESULT_TO, params.getIndexTo())
                        .queryParamIfPresent(WsReimbursementExternal.PARAM_DATE_FROM, Optional.ofNullable(params.getDateFrom()))
                        .queryParamIfPresent(WsReimbursementExternal.PARAM_DATE_TO, Optional.ofNullable(params.getDateTo()))
                        .queryParamIfPresent(WsReimbursementExternal.PARAM_MINIMUM_AMOUNT, Optional.ofNullable(params.getAmountFrom()))
                        .queryParamIfPresent(WsReimbursementExternal.PARAM_MAXIMUM_AMOUNT, Optional.ofNullable(params.getAmountTo()))
                        .build()
                        .toUri();

                log.info("WS " + WS_LIST_REIMBURSEMENTS_LABEL + " URI is ready: " + wsUri.toString());

                log.info("Checking HTTP method for WS " + WS_LIST_REIMBURSEMENTS_LABEL);

                HttpMethod wsMethod = wsReimbursementsListExternalConfig.get().getMethod();

                // test on ws method
                if (Objects.nonNull(wsMethod)) {

                    log.info("HTTP method for WS " + WS_LIST_REIMBURSEMENTS_LABEL + " is : " + wsMethod);

                    boolean isHttpMethodEqualsGet = wsMethod.equals(HttpMethod.GET);

                    if (isHttpMethodEqualsGet) {

                        log.info("Calling external WS... " + WS_LIST_REIMBURSEMENTS_LABEL);

                        ResponseEntity<WsListReimbursementsExternal> wsExternalResponse = restUtils.prepareGetWs(
                                wsUri, WsListReimbursementsExternal.class);

                        HttpStatus wsExternalResponseStatus = wsExternalResponse.getStatusCode();

                        if (wsExternalResponseStatus == HttpStatus.OK) {

                            WsListReimbursementsExternal externalWsResponseBody = wsExternalResponse.getBody();

                            if (Objects.nonNull(externalWsResponseBody)) {

                                ObjectMapper objectMapper = new ObjectMapper();
                                ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
                                String wsResponsePrettified = objectWriter.writeValueAsString(externalWsResponseBody);

                                log.info("External WS " + WS_LIST_REIMBURSEMENTS_LABEL + " response: \n".concat(wsResponsePrettified));

                                if (externalWsResponseBody.isSuccess()) {
                                    log.info("Formatting WS " + WS_LIST_REIMBURSEMENTS_LABEL + " data for client-side..");

                                    ReimbursementsListDefinitionInternal wsResponseInternal = new ReimbursementsListDefinitionInternal();
                                    wsResponseInternal.setTotalResults(externalWsResponseBody.getTotalResults());

                                    List<ReimbursementsListItemInternal> reimbursementsListFormatted = externalWsResponseBody
                                            .getItems()
                                            .stream()
                                            .map(item -> {

                                                ReimbursementsListItemInternal targetItem = new ReimbursementsListItemInternal();

                                                BeanUtils.copyProperties(item, targetItem);

                                                targetItem.setId(item.getId());

                                                return targetItem;
                                            })
                                            .collect(Collectors.toList());

                                    wsResponseInternal.setReimbursements(reimbursementsListFormatted);
                                    wsResponseInternal.setSuccess(true);

                                    log.info("Formatting data for client-side has finished!");

                                    return wsResponseInternal;

                                } else {
                                    exceptionMsg = "WS External " + WS_LIST_REIMBURSEMENTS_LABEL + " has returned an error msg:";

                                    if (Objects.nonNull(externalWsResponseBody.getErrorMsgs())) {
                                        exceptionMsg = externalWsResponseBody.getErrorMsgs()[0];
                                    }

                                    log.error(exceptionMsg);

                                    throw new OccuredErrorException(exceptionMsg);
                                }
                            } else {
                                exceptionMsg = "WS External " + WS_LIST_REIMBURSEMENTS_LABEL + " response body is invalid";
                                log.error(exceptionMsg);

                                throw new OccuredErrorException(exceptionMsg);
                            }
                        } else {
                            exceptionMsg = "WS External " + WS_LIST_REIMBURSEMENTS_LABEL + " call didn't pass!";
                            throw new ExternalWsNoResponseException(exceptionMsg);
                        }
                    } else {
                        exceptionMsg = "WS " + WS_LIST_REIMBURSEMENTS_LABEL + " Http method must be GET!";
                        log.error(exceptionMsg);

                        throw new OccuredErrorException(exceptionMsg);
                    }
                } else {
                    exceptionMsg = "WS " + WS_LIST_REIMBURSEMENTS_LABEL + " doesn't possess http method";
                    log.error(exceptionMsg);

                    throw new OccuredErrorException(exceptionMsg);
                }

            } else {
                exceptionMsg = "WS " + WS_LIST_REIMBURSEMENTS_LABEL + " config object doesn't exist!";
                log.error(exceptionMsg);

                throw new OccuredErrorException(exceptionMsg);
            }

        } catch (Exception e) {
            throw new OccuredErrorException("An error has occured while processing the request.");
        }
    }
}
