package com.igatn.extranet.rest.notifications.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationConfigObject {
    private String title;
    private String body;
}
