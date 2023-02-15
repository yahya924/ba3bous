package com.igatn.extranet.rest.notifications.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.igatn.extranet.domainjpa.api.data.NotificationRepository;
import com.igatn.extranet.domainjpa.impl.domain.notification.Notification;
import com.igatn.extranet.domainjpa.impl.domain.notification.Notification.NotificationType;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.notifications.NotificationTranslationConstants;
import com.igatn.extranet.rest.notifications.model.NotificationWsItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<NotificationWsItem> getAll(long userId) {
        
        log.info("Querying notifications for user with ID: "+ userId );

        Iterable<Notification> userNotifications = notificationRepository.findAllByRecipientId(userId);
        
        boolean hasAtLeastOneItem = userNotifications.iterator().hasNext();

        if (hasAtLeastOneItem) {

            log.info("Notification(s) found! \n");
            
            log.info("Preparing suitable response for client-side has started.. ");

            List<NotificationWsItem> notificationWsItems = new ArrayList<>();

            userNotifications.forEach( n -> {
                
                LocalDateTime dateFrom = LocalDateTime.now().minusMonths(1);
                LocalDateTime dateTo = LocalDateTime.now();

                LocalDateTime notifCreationDate = LocalDateTime.from(n.getCreatedAt());

                boolean isCreatedBeforeDateTo = notifCreationDate.isBefore(dateTo);
                boolean isCreatedAfterDateFrom = notifCreationDate.isAfter(dateFrom);
                boolean isCreationDateInInterval = isCreatedAfterDateFrom && isCreatedBeforeDateTo;

                if (isCreationDateInInterval) {

                    NotificationWsItem notificationWsItem = new NotificationWsItem();

                    notificationWsItem.setId(n.getId());
                    notificationWsItem.setSeen(n.getSeen());
                    LocalDateTime createdAt = n.getCreatedAt();

                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    String createdAtDateFormatted = dateTimeFormatter.format(createdAt);

                    notificationWsItem.setCreationDate(createdAtDateFormatted);

                    notificationWsItem.setDescription(n.getMessage());
                    notificationWsItem.setTitle(n.getTitle());
                    notificationWsItem.setType(n.getType().toString());

                    notificationWsItems.add(notificationWsItem);
                }
            });
            
            // FRE - revering list in order to make recent items on top
            Collections.reverse(notificationWsItems);

            try {
                ObjectWriter objectWriter = new ObjectMapper().writer();
                String resultAsJson = objectWriter
                        .withDefaultPrettyPrinter()
                        .writeValueAsString(notificationWsItems);
                
            log.info("Response is ready to go to client-side! \n" + resultAsJson);

            return notificationWsItems;

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        
        throw new IllegalArgumentException("Something went wrong when processing notifications for user: " + userId);
    }
    
    @Override
    public Notification createInstanceByType(NotificationType type, User user) {

        Notification newNotification = new Notification(user);

        switch (type) {
            case PR -> {
                newNotification.setTitle(NotificationTranslationConstants.NEW_PREMIUM_TITLE);
                newNotification.setMessage(NotificationTranslationConstants.NEW_PREMIUM_DESCRIPTION);
                newNotification.setType(NotificationType.PR);
            }
            case RM -> {
                newNotification.setTitle(NotificationTranslationConstants.NEW_REIMBURSEMENT_TITLE);
                newNotification.setMessage(NotificationTranslationConstants.NEW_REIMBURSEMENT_DESCRIPTION);
                newNotification.setType(NotificationType.RM);
            }
            case PL -> {
                newNotification.setTitle(NotificationTranslationConstants.RENEW_POLICY_TITLE);
                newNotification.setMessage(NotificationTranslationConstants.RENEW_POLICY_DESCRIPTION);
                newNotification.setType(NotificationType.PL);
            }
        }

        return newNotification;
    }
}