package com.igatn.extranet.rest.notifications.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * FRE - insured notifications list definition model
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class NotificationsDefinition {
    private final boolean success;
    private final List<NotificationWsItem> notifications;
}

