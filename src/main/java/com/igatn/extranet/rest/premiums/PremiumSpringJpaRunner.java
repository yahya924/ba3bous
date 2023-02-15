package com.igatn.extranet.rest.premiums;

/**
 * FRE - this class contains the persistence initialization required 
 * for REST WS list premiums
 */
//@Profile(value = {"develop"})
//@Configuration
//public class PremiumSpringJpaRunner {
//
//    @Autowired
//    ClientRepository clientRepository;
//
//    @Autowired
//    ExternalWsApiRepository externalWsApiRepository;
//    
//    @Autowired
//    WsConfigRepository wsConfigRepository;
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
//            final String DATE_NOW = SimpleDateFormatter.formatDate(Instant.now());
//
//            // ----------------- ws host
//            final String EXTERNAL_API_HOSTNAME = "192.168.216.17";
//            final int EXTERNAL_API_PORT = 5700;
//            final String EXTERNAL_API_MAIN_PATH = "/api";
//            final String EXTERNAL_API_LABEL = "Insurance External System";
//
//            final ExternalWsApi externalApi = ExternalWsApi.externalWsApiBuilder()
//                    .label(EXTERNAL_API_LABEL)
//                    .protocol(WsExternal.PROTOCOL_HTTP)
//                    .hostname(EXTERNAL_API_HOSTNAME)
//                    .port(EXTERNAL_API_PORT)
//                    .rootPath(EXTERNAL_API_MAIN_PATH)
//                    .createdAt(DATE_NOW)
//                    .create();
//
//            // ----------------- ws Config
//            final String wsPath = "/"
//                    .concat(WsPremiumExternal.BASE_PATH)
//                    .concat("/")
//                    .concat(WsPremiumExternal.LIST_PATH);
//
//            final String EXTERNAL_WS_LABEL = "Insured premiums";
//
//            final WsConfig wsPremiumsExternalConfig = WsConfig.wsConfigBuilder()
//                    .label(EXTERNAL_WS_LABEL)
//                    .host(externalApi)
//                    .method(HttpMethod.GET)
//                    .mediaType(MediaType.APPLICATION_JSON.toString())
//                    .path(wsPath)
//                    .createdAt(DATE_NOW)
//                    .create();
//            
//            // ----------------- client
//            final String CLIENT_NAME = "IGA Gestion";
//            Client client = new Client();
//            client.setName(CLIENT_NAME);
//            client.setExternalWsApi(externalApi);
//            
//            //------------ notification config for user
//            NotificationConfig notificationConfig = new NotificationConfig();
//            notificationConfig.setActive(false);
//            notificationConfig.setCreatedAt(Instant.now());
//            notificationConfig.setUpdatedAt(Instant.now());
//
//            //--------------- user
//            User user1 = new User();
//            user1.setClient(client);
//            user1.setRole("Insured");
//            user1.setUsername("340137@iga.tn");
//            user1.setPassword("6YX7J9TU");
//            user1.setUpdatedAt(Instant.now());
//            user1.setNotificationConfig(notificationConfig);

            // persistance ...
//            externalWsApiRepository.save(externalApi);
//            wsConfigRepository.save(wsPremiumsExternalConfig);
//            clientRepository.save(client);
//            notificationConfigRepository.save(notificationConfig);
//            userRepository.save(user1);
//            clientRepository.save(client);

//        };
//    }
//}
