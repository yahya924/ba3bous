package com.igatn.extranet.rest;

import com.igatn.extranet.service.history.AuthHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Additional config for rest template
 */
@Slf4j
@Configuration
public class RestClientConfig {
    
    /**
     * FRE - Declaring instance of rest template. This object is a support to consume external 
     * Rest API from backend check this: https://www.baeldung.com/rest-template
     */
    @Bean
    public RestTemplate restTemplate(AuthHistoryService authHistoryService) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors()
            .add(
                new RestClientUpdater(authHistoryService)
            );
        return restTemplate;
    }
}
