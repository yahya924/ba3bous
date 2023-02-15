package com.igatn.extranet.rest.premiums.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igatn.extranet.domainjpa.api.data.ws.ExternalWsApiRepository;
import com.igatn.extranet.domainjpa.api.data.ws.WsConfigRepository;
import com.igatn.extranet.domainjpa.impl.domain.client.Client;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.domainjpa.impl.domain.ws.WsExternal.WsPremiumExternal;
import com.igatn.extranet.domainjpa.impl.domain.ws.WsExternal.WsPremiumExternal.WsListPremiumsExternal;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.ExternalWsApi;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.WsConfig;
import com.igatn.extranet.rest.exceptions.ClientNotFoundException;
import com.igatn.extranet.rest.exceptions.UnparsableDateFormatException;
import com.igatn.extranet.rest.exceptions.ws.ExternalWsNoResponseException;
import com.igatn.extranet.rest.premiums.model.WsPremiumDefinition;
import com.igatn.extranet.rest.premiums.model.WsPremiumDefinition.WsListPremiumsParamsInternal;
import com.igatn.extranet.rest.premiums.model.WsPremiumDefinition.WsListPremiumsResponseInternal;
import com.igatn.extranet.rest.premiums.model.WsPremiumDefinition.WsPremiumItemInternal;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class PremiumServiceImpl implements PremiumService {

    @Autowired
    private RestUtils restUtils;

    @Autowired
    private ExternalWsApiRepository externalWsApiRepository;

    @Autowired
    private WsConfigRepository wsConfigRepository;
    
            
    @Override
    public WsListPremiumsResponseInternal getAll(WsListPremiumsParamsInternal params, User user) {
        
        String exceptionMsg = "";
        
        try {
            
            // get client
            Client client = user.getClient();

            // get ws host
            ExternalWsApi externalWsApi = externalWsApiRepository.findById(client.getId()).
                    orElseThrow(() -> new ClientNotFoundException("client not found!"));
            
            // get ws
            String wsPremiumsListPath =  "/"+WsPremiumExternal.BASE_PATH + "/" + WsPremiumExternal.LIST_PATH;
            
            Optional<WsConfig> wsPremiumsList = wsConfigRepository.findByHostIdAndPath(externalWsApi.getId(),wsPremiumsListPath);
            
            if(wsPremiumsList.isPresent()){

                WsConfig wsPremiumsListConfig = wsPremiumsList.get();

                StringBuilder wsPathBuilder = new StringBuilder();
                wsPathBuilder.append(externalWsApi.getRootPath());
                wsPathBuilder.append(wsPremiumsListConfig.getPath());

                URI wsUri = UriComponentsBuilder
                    .fromUri(externalWsApi.getAsUri())
                    .replacePath(wsPathBuilder.toString())
                    .queryParam(WsPremiumExternal.PARAM_LANGUAGE, params.getLanguage())
                    .queryParam(WsPremiumExternal.PARAM_RESULT_FROM, params.getIndexFrom())
                    .queryParam(WsPremiumExternal.PARAM_RESULT_TO, params.getIndexTo())
                    .queryParamIfPresent(WsPremiumExternal.PARAM_STATUS, Optional.ofNullable(params.getStatus()))
                    .build()
                    .toUri();

                HttpMethod wsMethod = wsPremiumsList.get().getMethod();

                // test on ws method
                if(wsMethod != null) {
                    ResponseEntity<WsListPremiumsExternal> wsExternalResponse = restUtils.prepareGetWs(
                            wsUri,  WsListPremiumsExternal.class);

                    HttpStatus wsExternalResponseStatus = wsExternalResponse.getStatusCode();

                    if (wsExternalResponseStatus == HttpStatus.OK) {

                        WsListPremiumsExternal externalWsResponseBody = wsExternalResponse.getBody();

                        if (Objects.nonNull(externalWsResponseBody)) {
                            ObjectMapper objectMapper = new ObjectMapper();

                            log.info("External WS Premiums list Response: \n".concat(objectMapper.writeValueAsString(externalWsResponseBody)));

                            log.info("Formatting data for client-side..");

                            WsListPremiumsResponseInternal wsResponseInternal = new WsListPremiumsResponseInternal();
                            
                            wsResponseInternal.setTotalResults(externalWsResponseBody.getTotalResults());
                            wsResponseInternal.setTotalAmountToPay(externalWsResponseBody.getTotalAmountToPay());

                            List<WsPremiumItemInternal> premiumsListFormatted = externalWsResponseBody
                                .getItems()
                                .stream()
                                .map(item -> {
                                    WsPremiumItemInternal targetItem = new WsPremiumItemInternal();
                                    BeanUtils.copyProperties(item, targetItem);
                                    targetItem.setId(Long.parseLong(item.getId()));
                                    
                                    targetItem.setInterval(Objects.equals(item.getStatus(), "PN") ? 
                                            getInterval(item.getFrom(), item.getTo()) : 
                                            new ArrayList<>());
                                    
                                    return targetItem;
                                })
                                .collect(Collectors.toList());

                            wsResponseInternal.setPremiums(premiumsListFormatted);

                            return wsResponseInternal;
                        } else {
                            exceptionMsg = "WS External premiums list response body is invalid";
                            log.info(exceptionMsg);
                        }
                    } else {
                        exceptionMsg = "WS External premiums list response is not ok!";
                        throw new ExternalWsNoResponseException(exceptionMsg);
                    }
                } else {
                    exceptionMsg = "WS method doesn't possess http method";
                    log.info(exceptionMsg);
                }
            } else {
                exceptionMsg = "WsPremiums response object is invalid!";
                log.info(exceptionMsg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } 

        return null;
    }
    
    private List<WsPremiumDefinition.ItemIntervalExpressionElement> getInterval(String from, String to) {

        String DateFormat = "dd/MM/yyyy";
        
        String i18nCommonIntervalPrefix = "Commun.interval.";
        
        String i18nFromIdPrefix = i18nCommonIntervalPrefix.concat("from");
        String i18nToIdPrefix = i18nCommonIntervalPrefix.concat("to");

        try {
            Date dateFrom = new SimpleDateFormat(DateFormat).parse(from);
            Date dateTo = new SimpleDateFormat(DateFormat).parse(to);

            return Stream.concat(getDateElements(dateFrom, i18nFromIdPrefix).stream(), getDateElements(dateTo, i18nToIdPrefix).stream()).toList();
        } catch (ParseException e) {
            
            throw new UnparsableDateFormatException("The date returned by the third party API is not valid.");
        }
    }
    
    private List<WsPremiumDefinition.ItemIntervalExpressionElement> getDateElements(Date date, String i18nIdPrefix) {
        
        String i18nMonthIdPrefix = "Commun.months.";

        DateFormat dayFormatter = new SimpleDateFormat("dd", new Locale("en"));
        DateFormat monthFormatter = new SimpleDateFormat("MMMM", new Locale("en"));
        DateFormat yearFormatter = new SimpleDateFormat("yyyy", new Locale("en"));

        return Arrays.asList(
                new WsPremiumDefinition.ItemIntervalExpressionElement(i18nIdPrefix, true),
                new WsPremiumDefinition.ItemIntervalExpressionElement(dayFormatter.format(date), false),
                new WsPremiumDefinition.ItemIntervalExpressionElement(i18nMonthIdPrefix.concat(monthFormatter.format(date).toLowerCase()), true),
                new WsPremiumDefinition.ItemIntervalExpressionElement(yearFormatter.format(date), false)
        );
    }
}
