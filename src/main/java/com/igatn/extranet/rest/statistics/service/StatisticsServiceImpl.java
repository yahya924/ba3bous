package com.igatn.extranet.rest.statistics.service;


import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.exceptions.ws.ExternalWsNoResponseException;
import com.igatn.extranet.rest.exchange.model.WsExchangesDefinition.*;
import com.igatn.extranet.rest.exchange.service.ExchangeService;
import com.igatn.extranet.rest.premiums.model.WsPremiumDefinition.WsListPremiumsParamsInternal;
import com.igatn.extranet.rest.premiums.model.WsPremiumDefinition.WsListPremiumsResponseInternal;
import com.igatn.extranet.rest.premiums.service.PremiumService;
import com.igatn.extranet.rest.reimbursement.model.ReimbursementInternal.*;
import com.igatn.extranet.rest.reimbursement.service.ReimbursementService;
import com.igatn.extranet.rest.statistics.model.WsStatisticsDefinition.WsListStatisticsResponseInternal;
import com.igatn.extranet.rest.statistics.model.WsStatisticsDefinition.WsListStatisticsParamsInternal;
import com.igatn.extranet.rest.statistics.model.WsStatisticsDefinition.WsStatisticsInternal;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    private PremiumService premiumService;

    @Autowired
    private ReimbursementService reimbursementService;

    @Autowired
    private ExchangeService exchangeService;

    @Override
    public WsListStatisticsResponseInternal getAll(WsListStatisticsParamsInternal params, User user) {
        ArrayList<WsStatisticsInternal> responseList = new ArrayList<>();
        if (params.isHasIndividualPolicies()) {

            WsListPremiumsParamsInternal paramsPremiums = WsListPremiumsParamsInternal.builder()
                    .language(params.getLanguage())
                    .indexFrom(0)
                    .indexTo(5)
                    .status("PN")
                    .build();
            log.info(paramsPremiums.toString());


            Optional<WsListPremiumsResponseInternal> wsListPremiumsResponse = Optional.ofNullable(premiumService.getAll(paramsPremiums, user));

            WsListPremiumsResponseInternal PremiumsResponse = wsListPremiumsResponse.orElseThrow(
                    () -> new ExternalWsNoResponseException("WS /statistics/getAll is not returning a valid response... PremiumsResponse is a NullPointer")
            );

            WsStatisticsInternal responsePremiumsIP = new WsStatisticsInternal();

            responsePremiumsIP.setId(3);
            responsePremiumsIP.setCode("premiums");
            responsePremiumsIP.setTitle("membership_fee_payable");
            responsePremiumsIP.setTotalResults(PremiumsResponse.getTotalResults());
            responseList.add(responsePremiumsIP);


        }

        ReimbursementsListParamsInternal paramsReimbursement = ReimbursementsListParamsInternal.builder()
                .language(params.getLanguage())
                .indexFrom(0)
                .indexTo(5)
                .status("NW")
                .build();

        Optional<ReimbursementsListDefinitionInternal> wsListReimbursementResponse = Optional.ofNullable(reimbursementService.getAll(paramsReimbursement, user));

        ReimbursementsListDefinitionInternal ReimbursementResponse = wsListReimbursementResponse.orElseThrow(
                () -> new ExternalWsNoResponseException("WS /statistics/getAll is not returning a valid response... ReimbursementResponse is a NullPointer")
        );

        WsStatisticsInternal responseReimbursementIP = new WsStatisticsInternal();

        responseReimbursementIP.setId(2);
        responseReimbursementIP.setCode("reimbursements");
        responseReimbursementIP.setTitle("reimbursements_in_progress");
        responseReimbursementIP.setTotalResults(ReimbursementResponse.getTotalResults());
        responseList.add(responseReimbursementIP);

        WsListExchangeParamsInternal paramsExchange = WsListExchangeParamsInternal.builder()
                .language(params.getLanguage())
                .indexFrom(0)
                .indexTo(5)
                .status("IP")
                .build();

        Optional<FormattedExchange> wsListExchangesResponse = Optional.ofNullable(exchangeService.getAll(paramsExchange, user));

        FormattedExchange ExchangesResponse = wsListExchangesResponse.orElseThrow(
                ()-> new ExternalWsNoResponseException("WS /statistics/getAll is not returning a valid response... ExchangesResponse is a NullPointer")
        );

        WsStatisticsInternal responseExchangeIP =  new WsStatisticsInternal();

        responseExchangeIP.setId(1);
        responseExchangeIP.setCode("exchanges");
        responseExchangeIP.setTitle("exchanges_in_progress");
        responseExchangeIP.setTotalResults(ExchangesResponse.getTotalResults());
        responseList.add(responseExchangeIP);


        WsListStatisticsResponseInternal response = new WsListStatisticsResponseInternal();
        response.setStatistics(responseList);




        return  response;
    }

}
