package com.igatn.extranet.rest.reimbursement;

import com.igatn.extranet.domainjpa.api.data.ws.ExternalWsApiRepository;
import com.igatn.extranet.domainjpa.api.data.ws.WsConfigRepository;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.ExternalWsApi;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.WsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.time.Instant;
import java.util.Optional;

/**
 * TODO - ELEMENT required for element #60669 - ws list reimbursements
 */
@Configuration
public class WsReimbursementSpringJpaRunner {
    
//    @Autowired
//    ExternalWsApiRepository externalWsApiRepository;
//
//    @Autowired
//    WsConfigRepository wsConfigRepository;
    
    @Bean
    public CommandLineRunner wsReimbursementLoader() {
//
        return args -> {
//
//            
            // ----------------- ws Config reimbursements list
//            ExternalWsApi externalWsApi = externalWsApiRepository.findByHostnameAndPort("192.168.216.17", 5700);
//
//            if (Objects.nonNull(externalWsApi)) {
//
//                final String DATE_NOW = SimpleDateFormatter.formatDate(Instant.now());
//
//                final String WS_PATH = "/"
//                        .concat(WsReimbursementExternal.BASE_PATH)
//                        .concat("/")
//                        .concat(WsReimbursementExternal.LIST_PATH);
//
//                final String EXTERNAL_WS_LABEL = "Insured reimbursements";
//
//                final WsConfig wsReimbursementsListExternal = WsConfig.wsConfigBuilder()
//                        .label(EXTERNAL_WS_LABEL)
//                        .host(externalWsApi)
//                        .method(HttpMethod.GET)
//                        .mediaType(MediaType.APPLICATION_JSON.toString())
//                        .path(WS_PATH)
//                        .createdAt(DATE_NOW)
//                        .create();
//
//                wsConfigRepository.save(wsReimbursementsListExternal);
//            }
        };
    }
}
