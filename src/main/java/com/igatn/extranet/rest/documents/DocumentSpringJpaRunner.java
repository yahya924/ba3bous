//package com.igatn.extranet.rest.documents;
//
//import com.igatn.extranet.domainjpa.api.data.ClientRepository;
//import com.igatn.extranet.domainjpa.api.data.NotificationConfigRepository;
//import com.igatn.extranet.domainjpa.api.data.UserRepository;
//import com.igatn.extranet.domainjpa.impl.domain.client.Client;
//import com.igatn.extranet.domainjpa.impl.domain.notification.NotificationConfig;
//import com.igatn.extranet.domainjpa.impl.domain.user.User;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Profile;
//
//import java.time.Instant;
//
///**
// * FRE - this class contains the persistence initialization required 
// * for REST WS list docs
// */
//@Profile(value = {"develop","test"})
//@Configuration
//public class DocumentSpringJpaRunner {
//
//    @Autowired
//    ClientRepository clientRepository;
//
//    @Autowired
//    UserRepository userRepository;
//
//    @Autowired
//    NotificationConfigRepository notificationConfigRepository;
//    
//    @Bean
//    public CommandLineRunner dataLoader() {
//        
//        return args -> {
//            
//            // prepare client
//            Client client1 = new Client();
//            client1.setName("Client A");
//            clientRepository.save(client1);
//
//            // prepare notification config for user
//            NotificationConfig notificationConfig = new NotificationConfig();
//            notificationConfig.setActive(false);
//            notificationConfig.setCreatedAt(Instant.now());
//            notificationConfig.setUpdatedAt(Instant.now());
//            notificationConfigRepository.save(notificationConfig);
//            
//            // prepare user
//            User user1 = new User();
//            user1.setClient(client1);
//            user1.setRole("Insured");
//            user1.setUsername("312244@iga.tn");
//            user1.setPassword("Iga12345!");
//            user1.setUpdatedAt(Instant.now());
//            user1.setNotificationConfig(notificationConfig);
//            
//            userRepository.save(user1);
//        };
//    }
//}
