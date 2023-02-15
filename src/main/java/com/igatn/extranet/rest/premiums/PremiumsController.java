package com.igatn.extranet.rest.premiums;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.exceptions.WsExternalApiNotFoundException;
import com.igatn.extranet.rest.premiums.model.WsPremiumDefinition.WsListPremiumsParamsInternal;
import com.igatn.extranet.rest.premiums.model.WsPremiumDefinition.WsListPremiumsResponseInternal;
import com.igatn.extranet.rest.premiums.service.PremiumService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@Slf4j
@RestController
@RequestMapping(path = "/premiums", produces = "application/json")
public class PremiumsController {

    @Autowired
    private PremiumService premiumService;
    
    /**
     * ASGHIR - test the controller
     *
     * @return
     */
    @GetMapping
    public String helloPremiums() {
        log.info("/premiums endpoint called!");

        return "Hello from /premiums";
    }

    /**
     * FRE - revert list premiums from ws external api
     * 
     * @param languageIsoCode
     * @param indexFrom
     * @param indexTo
     * @param statusCode
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping(path = "/getAll")
    public ResponseEntity<WsListPremiumsResponseInternal> all(
            Authentication authentication,
            // currently unused
            @RequestParam(value = "language", required = true, defaultValue = "fr") String languageIsoCode,
            @RequestParam(value = "skip", required = true, defaultValue = "0") Integer indexFrom,
            @RequestParam(value = "take", required = true, defaultValue = "5") Integer indexTo,
            @RequestParam(value = "status", required = false) String statusCode
    ) {

        log.info("-- /premiums/getAll endpoint called! --");

        try {
            WsListPremiumsParamsInternal params = WsListPremiumsParamsInternal.builder()
                    .language(languageIsoCode)
                    .indexTo(indexTo)
                    .indexFrom(indexFrom)
                    .status(statusCode)
                    .build();

            User user = (User) authentication.getPrincipal();
            
            WsListPremiumsResponseInternal wsListPremiumsResponse = premiumService.getAll(params,user);

            ResponseEntity<WsListPremiumsResponseInternal> response = new ResponseEntity<>(wsListPremiumsResponse, HttpStatus.OK);

            if (Objects.nonNull(response.getBody()))
                log.info("Sending response to client-side..: ".concat(response.getBody().toString()));

            return response;
            
        } catch (WsExternalApiNotFoundException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(new WsListPremiumsResponseInternal(), HttpStatus.NO_CONTENT);
    }
}

