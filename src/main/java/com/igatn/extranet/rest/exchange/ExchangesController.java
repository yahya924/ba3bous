package com.igatn.extranet.rest.exchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.exceptions.OccuredErrorException;
import com.igatn.extranet.rest.exceptions.ws.ExternalWsNoResponseException;
import com.igatn.extranet.rest.exchange.model.WsExchangesDefinition.*;
import com.igatn.extranet.rest.exchange.model.WsExchangesDefinition.WsListExchangeParamsInternal.*;
import com.igatn.extranet.rest.exchange.service.ExchangeService;
import org.springframework.security.core.Authentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

/**
 * FRE - Insured Exchanges, rest controller 
 */
@Slf4j
@RestController
@RequestMapping(path = "/exchanges", produces = "application/json")
public class ExchangesController {


    @Autowired
    private ExchangeService exchangeService;

    /**
     * test the controller
     *
     * @return
     */
    @GetMapping
    public String helloExchanges() {
        log.info("/exchanges endpoint called!");

        return "Hello from /exchanges";
    }

    /**
     * FRE - Revert exchange list with metadata
     * TODO - improve with @Service class, AOP, controls on errors and more..
     *
     * @param languageIsoCode
     * @param indexFrom
     * @param indexTo
     * @param statusCode
     * @param textToSearch
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping(path = "/getAll")
    public ResponseEntity<FormattedExchange> all(
            Authentication authentication,
            @RequestParam(value = "language", required = false, defaultValue = "fr") String languageIsoCode,
            @RequestParam(value = "skip", required = true, defaultValue = "0") Integer indexFrom,
            @RequestParam(value = "take", required = true, defaultValue = "5") Integer indexTo,
            @RequestParam(value = "status", required = false) String statusCode,
            @RequestParam(value = "text", required = false) String textToSearch,
            @RequestParam(value = "from", required = false) String dateFrom,
            @RequestParam(value = "to", required = false) String dateTo,
            @RequestParam(value = "direction", required = false) String direction
    ) {

        log.info("-- /exchanges/getAll endpoint called! --");

        log.info("Processing query for thrid party API started..");

        try {

            WsListExchangeParamsInternalBuilder params = WsListExchangeParamsInternal.builder()
                    .language(languageIsoCode)
                    .indexTo(indexTo)
                    .indexFrom(indexFrom);

            if (statusCode != null && !statusCode.equals("")) params.status(statusCode);

            if (StringUtils.hasText(dateFrom)) params.dateFrom(dateFrom);

            if (StringUtils.hasText(dateTo)) params.dateTo(dateTo);

            if (StringUtils.hasText(direction)) params.direction(direction);

            WsListExchangeParamsInternal wsParams = params.build();

            log.info("Processing query for thrid party API finished!");

            // using rest template from spring
            // check this: https://www.baeldung.com/rest-template
            // TODO - FRE - make control on response from third party api

            User user = (User) authentication.getPrincipal();

            FormattedExchange wsListResponse = exchangeService.getAll(wsParams, user);

            ResponseEntity<FormattedExchange> responseExchanges = new ResponseEntity<>(wsListResponse, HttpStatus.OK);

            Optional<FormattedExchange> responseBody = Optional.ofNullable(responseExchanges.getBody());

            FormattedExchange response = responseBody.orElseThrow(
                    () -> new ExternalWsNoResponseException("WS /exchanges/getAll is not returning a valid response... response is NullPointer")
            );

            return ResponseEntity.ok(response);


        } catch (Exception e) {
            throw new OccuredErrorException("An error has occured while processing your request.");
        }

    }
}
