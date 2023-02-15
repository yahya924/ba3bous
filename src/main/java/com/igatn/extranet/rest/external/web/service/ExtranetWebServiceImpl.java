package com.igatn.extranet.rest.external.web.service;

import com.igatn.extranet.domainjpa.api.data.UserRepository;
import com.igatn.extranet.domainjpa.api.data.demand.DemandRepository;
import com.igatn.extranet.domainjpa.api.data.demand.TypeDemandRepository;
import com.igatn.extranet.domainjpa.impl.domain.demand.Demand;
import com.igatn.extranet.domainjpa.impl.domain.demand.TypeDemand;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.exceptions.ReferentialValueNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class ExtranetWebServiceImpl implements ExtranetWebService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DemandRepository demandRepository;

    @Autowired
    private TypeDemandRepository typeDemandRepository;
    
    // FIXME: unused?
    private final String EXTERNAL_API_INVALID_RESPONSE_ERROR_MESSAGE = "The external API call is not returning a valid response !";
    
    /*
    This service was first developed to handle Update Password SAGILEA WS from the Extranet Mobile IS. This idea was abandoned so this service was commented,
    maybe it can serve for something else later. 
     */
//    @Override
//    public void updateExternalUserPassword(UpdatePasswordParameters parameters, String hostToken) {
//
//        /* 
//            This is a temporary solution because in our current database we don't have a ManyToOne association between the Client and ExternalWsApi entity,
//            which means a Client entity currently can't have multiple hosts. So if I try to fetch the host using the user's client, it will fetch the wrong one,
//            this is why I needed to fetch the host by label, until we can find the opportunity to change this and make the right tests on all the processes 
//            concerned by these changes. 
//         */
//        ExternalWsApi externalWsApi = externalWsApiRepository.findByLabelContains(SAGILEA_WCF_SERVICE_LABEL).
//                orElseThrow(ClientNotFoundException::new);
//
//        WsConfig wsUpdatePasswordConfig = wsConfigRepository.findByHostAndLabel(externalWsApi, INSURED_WEB_UPDATE_PASSWORD_LABEL)
//                .orElseThrow(() -> new WsExternalApiNotFoundException(INSURED_WEB_UPDATE_PASSWORD_LABEL + " external API was not found in the database."));
//        
//        if (wsUpdatePasswordConfig.getMethod() != HttpMethod.GET) {
//            throw new InvalidHttpMethodException();
//        }
//
//        StringBuilder wsUpdatePasswordUrl = new StringBuilder();
//        wsUpdatePasswordUrl.append(externalWsApi.getProtocol())
//                .append("://")
//                .append(externalWsApi.getHostname())
//                .append(":")
//                .append(externalWsApi.getPort())
//                .append(externalWsApi.getRootPath())
//                .append(wsUpdatePasswordConfig.getPath());
//
//        Map<String, String> queryParams = new HashMap<>();
//        queryParams.put(UPDATE_PASSWORD_WS_INSURED_ID_PARAMETER, parameters.getUserId());
//        queryParams.put(UPDATE_PASSWORD_WS_OLD_PASSWORD_PARAMETER, parameters.getOldPwd());
//        queryParams.put(UPDATE_PASSWORD_WS_NEW_PASSWORD_PARAMETER, parameters.getNewPwd());
//        queryParams.put(UPDATE_PASSWORD_WS_TOKEN_PARAMETER, hostToken);
//        queryParams.put(UPDATE_PASSWORD_WS_BROKER_ID_PARAMETER, parameters.getBrokerId());
//
//        Map<String, String> headers = new HashMap<>();
//
//        String noInterceptorHeaderAttribute = Optional.ofNullable(appService.getAppProperties().getSecurity().getHeaders().getNoInterceptorHeader())
//                .orElseThrow(() -> new SecurityHeaderNotFoundException("No Interceptor header"));
//
//        headers.put(noInterceptorHeaderAttribute, "*");
//
//        ResponseEntity<HostGenericResponse> sagileaApiResponse = restUtils.prepareGetWS(wsUpdatePasswordUrl.toString(), queryParams,
//                HostGenericResponse.class, headers);
//
//        HostGenericResponse response = Optional.ofNullable(sagileaApiResponse.getBody())
//                .orElseThrow(() -> new ExternalWsNoResponseException(EXTERNAL_API_INVALID_RESPONSE_ERROR_MESSAGE));
//
//        if (!response.getSuccess()) {
//            throw new ExternalWsNoResponseException(response.getMessage().length() > 0 ? response.getMessage() : EXTERNAL_API_INVALID_RESPONSE_ERROR_MESSAGE);
//        }
//
//        log.info("Updating insured password in SAGILEA DB has achieved successfully.");
//    }

    @Override
    public Boolean syncInternalUserPassword(User user, String newPassword) {
        
        user.setPassword(newPassword);
        
        userRepository.save(user);

        log.info("Updating insured password in Extranet Mobile DB has achieved successfully.");

        TypeDemand updatePasswordTypeDemand = Optional.ofNullable(typeDemandRepository.findByCode(UPDATE_PASSWORD_TYPE_DEMAND))
                .orElseThrow(() -> new ReferentialValueNotFoundException("Update Password Demand Type"));

        Demand demand = new Demand();
        demand.setType(updatePasswordTypeDemand);
        demand.setSender(user);
        demand.setMessage("");
        demand.setSubject("Mise à jour du mot de passe");
        
        demandRepository.save(demand);
        
        return true;
    }
}
