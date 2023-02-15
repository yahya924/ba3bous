package com.igatn.extranet.rest.notifications.model;

import com.igatn.extranet.domainjpa.impl.domain.notification.NotificationConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TokenRequest {
    private String token;
    private String os;
}
