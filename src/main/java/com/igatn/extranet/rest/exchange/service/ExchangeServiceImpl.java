package com.igatn.extranet.rest.exchange.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.igatn.extranet.domainjpa.api.data.ws.ExternalWsApiRepository;
import com.igatn.extranet.domainjpa.api.data.ws.WsConfigRepository;
import com.igatn.extranet.domainjpa.impl.domain.client.Client;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.ExternalWsApi;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.WsConfig;
import com.igatn.extranet.rest.exceptions.ClientNotFoundException;
import com.igatn.extranet.rest.exceptions.OccuredErrorException;
import com.igatn.extranet.rest.exceptions.ws.ExternalWsNoResponseException;
import com.igatn.extranet.rest.exchange.model.WsExchangesDefinition.*;
import com.igatn.extranet.rest.exchange.model.WsExchangesDefinition.WsListExchangeParamsInternal;
import com.igatn.extranet.rest.exchange.model.WsExchangesDefinition.ExchangeListDefinition;
import com.igatn.extranet.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class ExchangeServiceImpl implements ExchangeService {

    @Autowired
    private RestUtils restUtils;

    @Autowired
    private ExternalWsApiRepository externalWsApiRepository;

    @Autowired
    private WsConfigRepository wsConfigRepository;


    @Override
    public FormattedExchange getAll(WsListExchangeParamsInternal params , User user) {

        String exceptionMsg = "";

        try {

            Client client = user.getClient();
            log.info("Searching unique WS external API by client: " + client.getName());

            ExternalWsApi externalWsApi = externalWsApiRepository.findById(client.getId()).
                    orElseThrow(() -> new ClientNotFoundException("client not found!"));

            log.info("Found WS external API: " + externalWsApi.getLabel() + "!");

            String wsExchangesListPath = "/exchanges/getAll";

            Optional<WsConfig> wsExchangesListExternalConfig = wsConfigRepository
                    .findByHostIdAndPath(externalWsApi.getId(),wsExchangesListPath);

            WsConfig wsExchangesExternalConfig = wsExchangesListExternalConfig.orElseThrow(
                    () -> new OccuredErrorException("WS exchanges list config object doesn't exist!")
            );

            StringBuilder wsFullPathBuilder = new StringBuilder();
            wsFullPathBuilder.append(externalWsApi.getRootPath());
            wsFullPathBuilder.append(wsExchangesExternalConfig.getPath());
            String wsFullPath = wsFullPathBuilder.toString();

            log.info("Building URI to call WS  exchanges list" );

            URI wsUri = UriComponentsBuilder.
                    fromUri(externalWsApi.getAsUri())
                    .replacePath(wsFullPath)
                    .queryParam("language", params.getLanguage())
                    .queryParamIfPresent("status", Optional.ofNullable(params.getStatus()))
                    .queryParam("skip", params.getIndexFrom())
                    .queryParam("take", params.getIndexTo())
                    .queryParamIfPresent("dateFrom", Optional.ofNullable(params.getDateFrom()))
                    .queryParamIfPresent("dateTo", Optional.ofNullable(params.getDateTo()))
                    .queryParamIfPresent("direction", Optional.ofNullable(params.getDirection()))
                    .build()
                    .toUri();

            log.info("WS Exchanges list URI is ready: " + wsUri.toString());

            HttpMethod wsMethod = wsExchangesExternalConfig.getMethod();
            if (Objects.nonNull(wsMethod)) {
                log.info("HTTP method for WS  is Exchanges list: " + wsMethod);

                boolean isHttpMethodEqualsGet = wsMethod.equals(HttpMethod.GET);

                if (isHttpMethodEqualsGet) {
                    log.info("Calling external WS... Exchanges list");

                    ResponseEntity<ExchangeListDefinition> responseExchanges = restUtils.prepareGetWs(wsUri, ExchangeListDefinition.class);
                    HttpStatus wsExternalResponseStatus = responseExchanges.getStatusCode();

                    if (wsExternalResponseStatus == HttpStatus.OK) {
                        ExchangeListDefinition externalWsResponseBody = responseExchanges.getBody();

                        if (Objects.nonNull(externalWsResponseBody)) {

                            ObjectMapper objectMapper = new ObjectMapper();
                            ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
                            String wsResponsePrettified = objectWriter.writeValueAsString(externalWsResponseBody);

                            log.info("External WS Exchanges list response: \n".concat(wsResponsePrettified));

                            if (externalWsResponseBody.getSuccess()) {
                                log.info("Formatting WS Exchanges list data for client-side..");

                                FormattedExchange formattedExchange = new FormattedExchange();

                                formattedExchange.setSuccess(externalWsResponseBody.getSuccess());
                                formattedExchange.setTotalResults(externalWsResponseBody.getTotalResults());
                                formattedExchange.setExchanges(getFormattedItems(externalWsResponseBody.getItems()));

                                log.info("Formatting data for client-side has finished!");

                                return formattedExchange;
                            } else {
                                exceptionMsg = "WS External Exchanges list has returned an error msg:";

                                if (Objects.nonNull(externalWsResponseBody.getErrorMsgs())) {
                                    exceptionMsg = externalWsResponseBody.getErrorMsgs()[0];
                                }
                                log.error(exceptionMsg);

                                throw new OccuredErrorException(exceptionMsg);
                            }
                        } else {
                            exceptionMsg = "WS External Exchanges list response body is invalid";
                            log.error(exceptionMsg);

                            throw new OccuredErrorException(exceptionMsg);
                        }
                    } else {
                        exceptionMsg = "WS External Exchanges list call didn't pass!";
                        throw new ExternalWsNoResponseException(exceptionMsg);
                    }
                } else {
                    exceptionMsg = "WS Exchanges list Http method must be GET!";
                    log.error(exceptionMsg);

                    throw new OccuredErrorException(exceptionMsg);
                }
            } else {
                exceptionMsg = "WS Exchanges list doesn't possess http method";
                log.error(exceptionMsg);

                throw new OccuredErrorException(exceptionMsg);
            }
        } catch (Exception e) {
            throw new OccuredErrorException("An error has occured while processing your request.");
        }
    }

    public String translatedSource(String source) {
        return switch (source) {
            case "0" -> "Systeme";
            case "C" -> "Poste";
            case "F" -> "Fax";
            case "I" -> "Interne";
            case "M" -> "Mail";
            case "S" -> "SMS";
            case "T" -> "Phone";
            case "W" -> "Extranet";
            default -> "";
        };
    }

    private Exchange[] getFormattedItems (Exchange[] items){
        Arrays.stream(items).forEach(item-> item.setSource(translatedSource(item.getSource())));
        return items;
    }
}
