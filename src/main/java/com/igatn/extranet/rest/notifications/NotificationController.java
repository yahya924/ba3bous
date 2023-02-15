package com.igatn.extranet.rest.notifications;

import com.igatn.extranet.app.AppProperties;
import com.igatn.extranet.app.AppProperties.Notifications;
import com.igatn.extranet.app.AppService;
import com.igatn.extranet.domainjpa.api.data.NotificationConfigRepository;
import com.igatn.extranet.domainjpa.api.data.NotificationRepository;
import com.igatn.extranet.domainjpa.api.data.UserRepository;
import com.igatn.extranet.domainjpa.impl.domain.notification.Notification;
import com.igatn.extranet.domainjpa.impl.domain.notification.Notification.NotificationType;
import com.igatn.extranet.domainjpa.impl.domain.notification.NotificationConfig;
import com.igatn.extranet.domainjpa.impl.domain.notification.NotificationConfig.OsEnum;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.exceptions.EventNotFoundException;
import com.igatn.extranet.rest.exceptions.InvalidUserPlatformException;
import com.igatn.extranet.rest.exceptions.NoUserFoundByUsernameProvidedException;
import com.igatn.extranet.rest.exceptions.TargetNotFoundException;
import com.igatn.extranet.rest.models.ws.WsInternalResponse;
import com.igatn.extranet.rest.models.ws.WsResponse;
import com.igatn.extranet.rest.notifications.model.NotificationConfigObject;
import com.igatn.extranet.rest.notifications.model.NotificationDataObject;
import com.igatn.extranet.rest.notifications.model.NotificationMessageObject;
import com.igatn.extranet.rest.notifications.model.NotificationObject;
import com.igatn.extranet.rest.notifications.model.NotificationWsItem;
import com.igatn.extranet.rest.notifications.model.NotificationsDefinition;
import com.igatn.extranet.rest.notifications.model.SuccessResponse;
import com.igatn.extranet.rest.notifications.model.TokenRequest;
import com.igatn.extranet.rest.notifications.model.send.SendNotificationPayload;
import com.igatn.extranet.rest.notifications.model.send.SendNotificationResponse;
import com.igatn.extranet.rest.notifications.service.NotificationServiceImpl;
import com.igatn.extranet.utils.NotificationUtils;
import com.igatn.extranet.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.el.PropertyNotFoundException;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * OJA - Notification Controller
 */
@Slf4j
@RestController
@RequestMapping(path = NotificationConstants.REST_MAIN_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class NotificationController {

    @Autowired
    private RestUtils restUtils;
    
    @Autowired
    private NotificationUtils notificationUtils;

    @Autowired
    private NotificationConfigRepository notificationConfigRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    NotificationServiceImpl notificationService;

    @Autowired
    private AppService appService;
    
    /**
     * test the controller
     *
     * @return
     */
    @GetMapping
    public String helloNotifications() {
        log.info(NotificationConstants.REST_MAIN_PATH);

        return "Hello from " + NotificationConstants.REST_MAIN_PATH ;
    }

    /**
     * reverts notifications list
     * 
     * @param languageIsoCode
     * @return
     */
    @GetMapping(path = NotificationConstants.ENDPOINT_ALL)
    public ResponseEntity<NotificationsDefinition> all(
            Authentication userAuth,
            @RequestParam(value = "language", required = false) String languageIsoCode
    ) {

        final String API_PATH = NotificationConstants.ENDPOINT_ALL;

        log.info("-- "+
            NotificationConstants.REST_MAIN_PATH
                .concat(API_PATH)
                .concat(" endpoint called! -- \n")
        );

        User user = (User) userAuth.getPrincipal();
        long userId = user.getId();

        List<NotificationWsItem> wsNotifsListResult = notificationService.getAll(userId);
        
        List<NotificationWsItem> finalList = Optional.ofNullable(wsNotifsListResult)
                .orElse(Collections.emptyList());
        
        NotificationsDefinition notificationsDefinition = new NotificationsDefinition(true, finalList);

            return new ResponseEntity<>(notificationsDefinition, HttpStatus.OK);
    }
    
    private Notification prepareNotification(Notification notification, String evtType) {
        switch (evtType) {
        case "PR" -> {
            notification.setTitle(NotificationTranslationConstants.NEW_PREMIUM_TITLE);
            notification.setMessage(NotificationTranslationConstants.NEW_PREMIUM_DESCRIPTION);
            notification.setType(NotificationType.PR);
        }
        case "RM" -> {
            notification.setTitle(NotificationTranslationConstants.NEW_REIMBURSEMENT_TITLE);
            notification.setMessage(NotificationTranslationConstants.NEW_REIMBURSEMENT_DESCRIPTION);
            notification.setType(NotificationType.RM);
        }
        case "PL" -> {
            notification.setTitle(NotificationTranslationConstants.RENEW_POLICY_TITLE);
            notification.setMessage(NotificationTranslationConstants.RENEW_POLICY_DESCRIPTION);
            notification.setType(NotificationType.PL);
        }
        default -> {
            notification.setTitle("");
            notification.setMessage("");
        }
        }

        return notification;
    }
    
    /**
     * send notifications endpoint for temporary Web interface
     *
     * @return
     */
    @GetMapping(NotificationConstants.ENDPOINT_SEND)
    public ResponseEntity<?> sendNotification(
        @RequestParam(value = "username") String username,
        @RequestParam(value = "evtType") String evtType)
    {
        log.info("processing /notifications/send in progress.");

        User user = userRepository.findByUsername(username);

        if (user != null) {
            Notification notification = prepareNotification(new Notification(),evtType);

            if (notification.getType() != null) {
                notification.setRecipient(user);
                notification.setSeen(false);
                notification.setCreatedAt(LocalDateTime.now());
                notification.setUpdatedAt(LocalDateTime.now());

                notificationRepository.save(notification);

                NotificationConfig userNotificationConfig = user.getNotificationConfig();
                NotificationConfig.OsEnum configOs = userNotificationConfig.getOs();

                if (Boolean.TRUE.equals(userNotificationConfig.isActive()) && configOs != null) {
                    String userDeviceToken = userNotificationConfig.getToken();

                    if (Objects.equals(configOs.getLabel(), NotificationConfig.OsEnum.IOS.getLabel())) {
                        userDeviceToken = notificationUtils.getAPNSandboxRegistrationToken(userDeviceToken);
                    }

                    String accessToken = notificationUtils.authenticateWithGoogleCredentials();

                    String url = appService.getAppProperties().getNotifications().getUrl();

                    String noInterceptorHeaderAttribute = appService.getAppProperties().getSecurity().getHeaders().getNoInterceptorHeader();

                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + accessToken);
                    headers.put("Content-Type", "application/json");
                    headers.put(noInterceptorHeaderAttribute, "*");

                    String title = "MySeva Notification";
                    String body = "Vous avez reçu une nouvelle notification de la part de MySeva";

                    NotificationConfigObject notificationConfigObject = new NotificationConfigObject();
                    notificationConfigObject.setTitle(title);
                    notificationConfigObject.setBody(body);

                    NotificationDataObject notificationDataObject = new NotificationDataObject();
                    notificationDataObject.setTitle(title);
                    notificationDataObject.setMessage(body);

                    NotificationMessageObject notificationMessageObject = new NotificationMessageObject();
                    notificationMessageObject.setNotification(notificationConfigObject);
                    notificationMessageObject.setToken(userDeviceToken);
                    notificationMessageObject.setData(notificationDataObject);

                    NotificationObject notificationObject = new NotificationObject();
                    notificationObject.setMessage(notificationMessageObject);

                    restUtils.preparePostWS(url, new HashMap<>(), Object.class, notificationObject, headers);
                }

                SuccessResponse successResponse = new SuccessResponse(true);

                return ResponseEntity.ok(successResponse);
            }

            throw new EventNotFoundException();
        }

        throw new NoUserFoundByUsernameProvidedException();
    }

    /**
     * send notifications endpoint
     * 
     * TODO: Add "POST/send" rest path to ws docs
     * TODO: add description
     *
     * @param payload
     * @return {@link ResponseEntity<WsResponse>}
     */
    @PostMapping(NotificationConstants.ENDPOINT_SEND)
//    @RolesAllowed("INSURANCE_SYSTEM")
    public ResponseEntity<WsInternalResponse> sendNotification(
         @Valid @RequestBody SendNotificationPayload payload
    ) {
        String payloadType = payload.getEvtType();
        Set<@Email String> successList = new TreeSet<>();

        NotificationType notificationType;
        
        try {
            notificationType = NotificationType.valueOf(payloadType.toUpperCase(Locale.ROOT));
        } 
        catch (IllegalArgumentException e) {

            String err = "Unknown event type: " + payloadType;

            log.error(err);

            return ResponseEntity
                .badRequest()
                .body(WsInternalResponse.getErrorInstance(err));

        }

        payload
            .getUsernames()
            .forEach(
                payloadUserEmail ->
                    Optional
                    .ofNullable(userRepository.findByUsername(payloadUserEmail))
                    .ifPresentOrElse(
                        user -> {

                            final NotificationConfig NOTIFICATION_CONFIG = Optional
                                .of(user)
                                .map(User::getNotificationConfig)
                                .orElseThrow(() -> new TargetNotFoundException(
                                        "User doesn't posses \"notification-config\""
                                    )
                                );

                            if (NOTIFICATION_CONFIG.isActive()) {

                                log.info("Send notification processing for user  /send has been started.");

                                // part 1 : send
                                OsEnum configOs = Optional
                                    .of(NOTIFICATION_CONFIG)
                                    .map(NotificationConfig::getOs)
                                    .orElseThrow(() -> new TargetNotFoundException(
                                            "Object notification-config doesn't posses \"OS\" specification"
                                        )
                                    );

                                String userDeviceToken = Optional
                                    .of(NOTIFICATION_CONFIG)
                                    .map(NotificationConfig::getToken)
                                    .orElseThrow(() -> new TargetNotFoundException(
                                            "Object notification-config doesn't posses \"Token\" from provider(s)"
                                        )
                                    );

                                if (Objects.equals(configOs.getLabel(), OsEnum.IOS.getLabel())) {
                                    userDeviceToken = notificationUtils.getAPNSandboxRegistrationToken(userDeviceToken);
                                }

                                String accessToken = notificationUtils.authenticateWithGoogleCredentials();

                                String url = Optional
                                    .ofNullable(appService.getAppProperties())
                                    .map(AppProperties::getNotifications)
                                    .map(Notifications::getUrl)
                                    .orElseThrow(() -> new PropertyNotFoundException("Property notification's config URL is not found!"));

                                Map<String, String> headers = new HashMap<>();
                                headers.put("Authorization", "Bearer " + accessToken);
                                headers.put("Content-Type", "application/json");

                                // TODO: translation?
                                String title = "MySeva Notification";
                                String body = "Vous avez reçu une nouvelle notification de la part de MySeva";

                                NotificationConfigObject notificationConfigObject = new NotificationConfigObject();
                                notificationConfigObject.setTitle(title);
                                notificationConfigObject.setBody(body);

                                NotificationDataObject notificationDataObject = new NotificationDataObject();
                                notificationDataObject.setTitle(title);
                                notificationDataObject.setMessage(body);

                                NotificationMessageObject notificationMessageObject = new NotificationMessageObject();
                                notificationMessageObject.setNotification(notificationConfigObject);
                                notificationMessageObject.setToken(userDeviceToken);
                                notificationMessageObject.setData(notificationDataObject);

                                NotificationObject notificationObject = new NotificationObject();
                                notificationObject.setMessage(notificationMessageObject);

                                try {
                                    restUtils.preparePostWS(url, new HashMap<>(), Object.class, notificationObject, headers);
                                } catch (HttpClientErrorException exception) {
                                    log.error(
                                        "User with 'username': " + payloadUserEmail +
                                            " got an error while sending notification"
                                    );
                                }
                                // part 2: persist
                                // create notification instance for client-side
                                Notification notification = notificationService.createInstanceByType(notificationType, user);

                                notificationRepository.save(notification);

                                // add to success list
                                successList.add(payloadUserEmail);

                            } else {
                                log.warn(
                                    "User with 'username': " + payloadUserEmail + " " +
                                        "has no permission to get notifications"
                                );
                            }
                        },
                        () -> log.warn("No user found with 'username':" + payloadUserEmail)
                    )
            );

        if (successList.isEmpty())
            return ResponseEntity.ok(
                WsInternalResponse
                    .getSuccessInstance(
                        "No one of the following users has notification activated"
                    )
            );

        return new ResponseEntity<>(
            SendNotificationResponse.getSuccessInstance(
                "Notification has been sent to following 'usernames'", successList),
            null,
            HttpStatus.CREATED
        );

    }
    

    /**
     * OJA - active/deactivate user's notifications
     *
     * @return
     */
    @PostMapping(path = NotificationConstants.ENDPOINT_TOGGLE_NOTIFICATIONS)
    public ResponseEntity<?> toggleNotifications(Authentication auth) {

        log.info("-- /notifications/toggleNotifications endpoint reached! -- \n");

        User user = (User) auth.getPrincipal();

        NotificationConfig notificationConfig = user.getNotificationConfig();

        notificationConfig.setActive(!notificationConfig.isActive());

        notificationConfigRepository.save(notificationConfig);

        SuccessResponse response = new SuccessResponse(true);

        response.setSuccess(true);

        return ResponseEntity.ok(response);
    }

    /**
     * update token endpoint
     *
     * @param tokenRequest
     * @return
     */
    @PostMapping(NotificationConstants.ENDPOINT_UPDATE_TOKEN)
    public ResponseEntity<SuccessResponse> updateToken(@RequestBody TokenRequest tokenRequest, Authentication auth) {
        
        log.info("processing /notifications/updateToken in progress.");

        User user = (User) auth.getPrincipal();

        NotificationConfig notificationConfig = user.getNotificationConfig();
        
        List<NotificationConfig.OsEnum> operatingSystems = new ArrayList<>();
        operatingSystems.add(NotificationConfig.OsEnum.ANDROID);
        operatingSystems.add(NotificationConfig.OsEnum.IOS);

        NotificationConfig.OsEnum matchingOs = operatingSystems.stream().filter(os -> tokenRequest.getOs().equals(os.getLabel())).findFirst().orElse(null);
        
        if (matchingOs != null) {
            notificationConfig.setToken(tokenRequest.getToken());
            notificationConfig.setOs(matchingOs);
            notificationConfig.setUpdatedAt(Instant.now());

            notificationConfigRepository.save(notificationConfig);

            SuccessResponse successResponse = new SuccessResponse(true);

            return ResponseEntity.ok(successResponse);
        }
        
        throw new InvalidUserPlatformException();
    }


    /**
     * inform backend that user has seen some notifications
     * 
     * @return {@link ResponseEntity<WsInternalResponse>}
     */
    @PostMapping(NotificationConstants.ENDPOINT_SEE)
    public ResponseEntity<WsInternalResponse> see(){

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // covert Iterable to List
        // TODO: change return type of findAllByRecipientId() in jpa project to List
        List<Notification> notifications = StreamSupport
            .stream(
                notificationRepository
                    .findAllByRecipientId(user.getId())
                    .spliterator(), 
                false
            )
            .collect(Collectors.toList());
            
        if (notifications.isEmpty())
            return ResponseEntity.ok(
                WsInternalResponse
                    .getErrorInstance(
                        "No Notifications found for user with ID: " + user.getId()
                    )
            );
        
        notifications.forEach(
            notification -> {
                if(!notification.getSeen())
                    notification.markSeen();
            });
        
        notificationRepository.saveAll(notifications);

        return new ResponseEntity<>(
            WsInternalResponse.getSuccessInstance(
                "Notifications for user with ID: "+user.getId()+" has been marked seen"
            ),
            null,
            HttpStatus.CREATED
        );
    }
}