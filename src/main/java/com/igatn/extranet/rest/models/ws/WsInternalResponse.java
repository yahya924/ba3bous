package com.igatn.extranet.rest.models.ws;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class WsInternalResponse extends WsResponse {
    
    @Nullable
    protected String message;

    public WsInternalResponse(@Nullable String message) {
        this.message = message;
    }
    
    private WsInternalResponse(@Nullable String message, boolean isSuccess) {
        this.success = isSuccess;
        this.message = message;
    }

    public static WsInternalResponse getSuccessInstance(@NotBlank String successMsg){
        return new WsInternalResponse(successMsg);
    }

    public static WsInternalResponse getErrorInstance(@NotBlank String errorMsg){
        return new WsInternalResponse(errorMsg,false);
    }

}
