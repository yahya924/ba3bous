package com.igatn.extranet.utils;

import com.google.auth.oauth2.GoogleCredentials;
import com.igatn.extranet.app.AppService;
import com.igatn.extranet.rest.exceptions.APNSandboxInvalidResponseException;
import com.igatn.extranet.rest.exceptions.NoNotificationServiceConfigException;
import com.igatn.extranet.rest.notifications.model.APNSandboxRequestBody;
import com.igatn.extranet.rest.notifications.model.APNSandboxResponse;
import com.igatn.extranet.rest.notifications.model.APNSandboxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationUtils {

    @Autowired
    private AppService appService;

    @Autowired
    private RestUtils restUtils;
    
    public String getAPNSandboxRegistrationToken(String token) {
        String apnSandboxUrl = appService.getAppProperties().getNotifications().getApnSandboxUrl();

        String noInterceptorHeaderAttribute = appService.getAppProperties().getSecurity().getHeaders().getNoInterceptorHeader();

        Map<String, String> apnSandboxHeaders = new HashMap<>();
        apnSandboxHeaders.put("Authorization", "key=" + appService.getAppProperties().getNotifications().getFirebaseServerKey());
        apnSandboxHeaders.put("Content-Type", "application/json");
        apnSandboxHeaders.put(noInterceptorHeaderAttribute, "*");

        APNSandboxRequestBody apnSandboxRequestBody = new APNSandboxRequestBody();
        apnSandboxRequestBody.setApplication(appService.getAppProperties().getNotifications().getIosBundleId());
        apnSandboxRequestBody.setSandbox(false);
        apnSandboxRequestBody.setApns_tokens(new String[] { token });

        ResponseEntity<APNSandboxResponse> apnSandboxResponseEntity = restUtils.preparePostWS(apnSandboxUrl, new HashMap<>(),
                APNSandboxResponse.class, apnSandboxRequestBody, apnSandboxHeaders);

        APNSandboxResponse apnSandboxResponse = apnSandboxResponseEntity.getBody();

        if (apnSandboxResponse != null) {
            APNSandboxResult apnSandboxResult = Arrays.stream(apnSandboxResponse.getResults())
                    .filter(result -> token.equals(result.getApns_token())).findFirst().orElse(null);

            if (apnSandboxResult != null) {
                return apnSandboxResult.getRegistration_token();
            }
            else {
                throw new APNSandboxInvalidResponseException();
            }
        }
        else {
            throw new APNSandboxInvalidResponseException();
        }
    }
    
    public String authenticateWithGoogleCredentials() {
//        Resource resource = new ClassPathResource("extranet-mobile-service-account.json");
        
        try {
            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(new FileInputStream(appService.getAppProperties().getNotifications().getConfigPath()))
//                    .fromStream(new FileInputStream(resource.getFile()))
                    .createScoped(List.of(appService.getAppProperties().getNotifications().getScope()));
            
            googleCredentials.refresh();
            
            return googleCredentials.getAccessToken().getTokenValue();
        } 
        catch (IOException e) {
            throw new NoNotificationServiceConfigException(e.getMessage());
        }
    }
}
