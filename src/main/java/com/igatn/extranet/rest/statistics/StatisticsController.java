package com.igatn.extranet.rest.statistics;


import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.exceptions.OccuredErrorException;
import com.igatn.extranet.rest.exceptions.ws.ExternalWsNoResponseException;
import com.igatn.extranet.rest.statistics.model.WsStatisticsDefinition.WsListStatisticsParamsInternal;
import com.igatn.extranet.rest.statistics.model.WsStatisticsDefinition.WsListStatisticsResponseInternal;
import com.igatn.extranet.rest.statistics.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(path = "/statistics", produces = "application/json")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping(path = "/getAll")
    public ResponseEntity<WsListStatisticsResponseInternal> all(
            Authentication authentication,
            // currently unused
            @RequestParam(value = "language", required = true, defaultValue = "fr") String languageIsoCode,
            @RequestParam(value = "hasIndividualPolicies", required = true, defaultValue = "false") boolean hasIndividualPolicies
    ) {
        log.info("-- /statistics/getAll endpoint called! --");

        try {
            WsListStatisticsParamsInternal params = WsListStatisticsParamsInternal.builder()
                    .language(languageIsoCode)
                    .hasIndividualPolicies(hasIndividualPolicies)
                    .build();

            User user = (User) authentication.getPrincipal();

            WsListStatisticsResponseInternal wsListStatisticsResponse = statisticsService.getAll(params, user);

            ResponseEntity<WsListStatisticsResponseInternal> statisticsResponse = new ResponseEntity<>(wsListStatisticsResponse, HttpStatus.OK);

            Optional<WsListStatisticsResponseInternal> responseBody = Optional.ofNullable(statisticsResponse.getBody());

            WsListStatisticsResponseInternal response = responseBody.orElseThrow(
                    () -> new ExternalWsNoResponseException("WS /statistics/getAll is not returning a valid response... response is NullPointer")
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new OccuredErrorException("An error has occured while processing your request.");
        }
    }
}
