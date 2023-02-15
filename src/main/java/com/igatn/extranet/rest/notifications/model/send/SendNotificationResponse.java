package com.igatn.extranet.rest.notifications.model.send;

import com.igatn.extranet.rest.models.ws.WsInternalResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class SendNotificationResponse extends WsInternalResponse {
    
    @NotNull
    private Set<@Email String> usernames;

    private SendNotificationResponse(@NotNull String message, @NotNull Set<@Email String> usernames) {
        this.message = message;
        this.usernames = usernames;
    }

    public static SendNotificationResponse getSuccessInstance(
        @NotBlank String successMsg,
        @NotNull Set<@Email String> usernames
    ){
        return new SendNotificationResponse(successMsg, usernames);
    }
    
}
