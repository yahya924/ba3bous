package com.igatn.extranet.rest.notifications.model.send;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
public class SendNotificationPayload {
    
    @NotEmpty(message = "The 'usernames' must not be empty")
    private Set<@Email String> usernames;
    
    @NotBlank(message = "The 'event-type' must not be empty")
    private String evtType;
}
