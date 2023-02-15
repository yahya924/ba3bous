package com.igatn.extranet.rest.models.ws;

import com.igatn.extranet.rest.exceptions.ws.ExternalWsNoResponseException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class WsSignInExternalResponse extends WsExternalResponse {
    
    private ExternalWsSignInItem item;
    
    public boolean hasData(){
        return item != null && StringUtils.hasText(item.token());
    }
    
    public String getExternalAuthToken(){
        return Optional.ofNullable(item.token())
            .orElseThrow(
                () -> new ExternalWsNoResponseException("No external auth token !")
            );
    }
}
