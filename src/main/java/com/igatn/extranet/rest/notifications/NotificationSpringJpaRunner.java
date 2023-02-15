//package com.igatn.extranet.rest.notifications;
//
//import com.igatn.extranet.domainjpa.api.data.NotificationRepository;
//import com.igatn.extranet.domainjpa.impl.domain.notification.Notification;
//import com.igatn.extranet.domainjpa.impl.domain.notification.Notification.NotificationType;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.transaction.Transactional;
//import java.util.List;
//
//@Slf4j
//@Configuration
//@Transactional
//public class NotificationSpringJpaRunner {
//
//
//    @Bean
//    public CommandLineRunner dataLoader(NotificationRepository notificationRepository) {
//
//        return args -> {
//            
//            List<Notification> allNotifications = (List) notificationRepository.findAll();
//
//            allNotifications = allNotifications
//                    .stream()
//                    .map(n -> {
//
//                        NotificationType eType = n.getType();
//
//                        String titleToUse = n.getTitle();
//                        String descriptionToUse = n.getMessage();
//
//                        switch (eType) {
//                        case PR -> {
//                                titleToUse = NotificationTranslationConstants.NEW_PREMIUM_TITLE;
//                                descriptionToUse = NotificationTranslationConstants.NEW_PREMIUM_DESCRIPTION;
//                            }
//                        case RM -> {
//                                titleToUse = NotificationTranslationConstants.NEW_REIMBURSEMENT_TITLE;
//                                descriptionToUse = NotificationTranslationConstants.NEW_REIMBURSEMENT_DESCRIPTION;
//                            }
//                        case PL -> {
//                                titleToUse = NotificationTranslationConstants.RENEW_POLICY_TITLE;
//                                descriptionToUse = NotificationTranslationConstants.RENEW_POLICY_DESCRIPTION;
//                            }
//                        }
//
//                        if(!n.getTitle().equals(titleToUse)) n.setTitle(titleToUse);
//                        if(!n.getMessage().equals(descriptionToUse)) n.setMessage(descriptionToUse);
//
//                        return n;
//                    })
//                    .toList();
//            
//            // TODO - for sprint 8
////            notificationRepository.saveAll(allNotifications);
//
//        };
//    }
//
//}
//
