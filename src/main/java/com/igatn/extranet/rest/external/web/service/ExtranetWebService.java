package com.igatn.extranet.rest.external.web.service;

import com.igatn.extranet.domainjpa.impl.domain.user.User;

/*
    Some attributes in this interface were first developed to handle Update Password SAGILEA WS from the Extranet Mobile IS. This idea was abandoned 
    so they were commented, maybe they can serve for something else later. 
*/
public interface ExtranetWebService {
    
//    String SAGILEA_WCF_SERVICE_LABEL = "SAGILEA WCF Service";
//    String INSURED_WEB_UPDATE_PASSWORD_LABEL = "Insured Web update password";
    String UPDATE_PASSWORD_TYPE_DEMAND = "PWD";
    
//    String UPDATE_PASSWORD_WS_INSURED_ID_PARAMETER = "idAssure";
//    String UPDATE_PASSWORD_WS_OLD_PASSWORD_PARAMETER = "ancienMotDePasse";
//    String UPDATE_PASSWORD_WS_NEW_PASSWORD_PARAMETER = "nouveauMotDePasse";
//    String UPDATE_PASSWORD_WS_TOKEN_PARAMETER = "token";
//    String UPDATE_PASSWORD_WS_BROKER_ID_PARAMETER = "brokerId";

//    void updateExternalUserPassword(UpdatePasswordParameters parameters, String hostToken);
    
    Boolean syncInternalUserPassword(User user, String newPassword);
}
