package com.igatn.extranet.rest.notifications.service;

import com.igatn.extranet.domainjpa.impl.domain.notification.Notification;
import com.igatn.extranet.domainjpa.impl.domain.notification.Notification.NotificationType;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.notifications.model.NotificationWsItem;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface NotificationService {
    
    @NotNull List<NotificationWsItem> getAll(long userId);
    
    @NotNull Notification createInstanceByType(@NotNull NotificationType type,@NotNull User user);
}
