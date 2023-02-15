package com.igatn.extranet.rest.user.models;

import com.igatn.extranet.rest.models.ws.WsInternalResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BasicAuthResponse extends WsInternalResponse {     
    
    private AuthStep currentStep;
    private AuthSubStep currentSubStep;
    
    public BasicAuthResponse(String message, AuthStep currentStep, AuthSubStep currentSubStep) {
        this.message = message;
        this.currentStep = currentStep;
        this.currentSubStep = currentSubStep;
    }
}
