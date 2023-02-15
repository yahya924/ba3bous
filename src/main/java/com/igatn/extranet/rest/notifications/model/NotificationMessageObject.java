package com.igatn.extranet.rest.notifications.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessageObject {
    private String token;
    private NotificationConfigObject notification;
    private Object data;
}
