package com.igatn.extranet.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.igatn.extranet.app.AppProperties.Security;
import com.igatn.extranet.app.AppProperties.ExternalApi;

/**
 * Custom properties service
 */
@Service
public class AppService {

    private final AppProperties appProperties;

    @Autowired
    public AppService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public AppProperties getAppProperties() {
        return this.appProperties;
    }

    public Security getAppSecurity() {
        return this.appProperties.getSecurity();
    }
    
    public ExternalApi getExternalApi() {
        return this.appProperties.getExternalApi();
    }
}
