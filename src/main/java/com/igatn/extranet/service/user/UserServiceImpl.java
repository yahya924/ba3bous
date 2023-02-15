package com.igatn.extranet.service.user;

import com.igatn.extranet.app.AppProperties;
import com.igatn.extranet.app.AppProperties.Security;
import com.igatn.extranet.app.AppProperties.Security.InMemoryUser;
import com.igatn.extranet.app.AppService;
import com.igatn.extranet.domainjpa.api.data.UserRepository;
import com.igatn.extranet.domainjpa.api.data.ws.ExternalWsApiRepository;
import com.igatn.extranet.domainjpa.impl.domain.client.Client;
import com.igatn.extranet.domainjpa.impl.domain.device.Device;
import com.igatn.extranet.domainjpa.impl.domain.tracking.AppAuthHistory;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.ExternalWsApi;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.WsConfig;
import com.igatn.extranet.rest.models.ws.WsSignInExternalResponse;
import com.igatn.extranet.rest.user.models.BasicAuthResponse;
import com.igatn.extranet.rest.user.models.BasicCredentials;
import com.igatn.extranet.service.externalws.ExternalWsService;
import com.igatn.extranet.service.history.AuthHistoryService;
import com.igatn.extranet.utils.ExtranetUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;

/**
 * FRE - User service
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    
    private final ExternalWsApiRepository externalWsApiRepository;
    
    private final UserRepository userRepository;
    
    private final ExternalWsService externalWsService;

    private final AuthHistoryService authHistoryService;
    
    private final AppService appService;
    
    public UserServiceImpl(
        ExternalWsApiRepository externalWsApiRepository,
        UserRepository userRepository,
        ExternalWsService externalWsService,
        AuthHistoryService authHistoryService,
        AppService appService
    ) {
        this.externalWsApiRepository = externalWsApiRepository;
        this.userRepository = userRepository;
        this.externalWsService = externalWsService;
        this.authHistoryService = authHistoryService;
        this.appService = appService;
    }

    // service methods
    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user != null) {
            return user;
        } 

        // temporary additional user for external insurance system
        Optional<InMemoryUser> inMemoryUserOptional = Optional
            .ofNullable(appService.getAppProperties())
            .map(AppProperties::getSecurity)
            .map(Security::getInMemoryUser);

        if(inMemoryUserOptional.isPresent()) {

            InMemoryUser inMemoryUser = inMemoryUserOptional.get();

            String inMemoryUsername = Optional
                .ofNullable(inMemoryUser.getUsername())
                .orElse("");

            if (inMemoryUsername.equalsIgnoreCase(username)) {

                String inMemoryPassword = inMemoryUser.getPassword();
                String inMemoryRole = inMemoryUser.getRole();

                user = new User();
                user.setId(-1L);
                user.setClient(null);
                user.setUsername(inMemoryUsername);
                user.setPassword(inMemoryPassword);
                user.setRole(inMemoryRole);

                return user;
            }
        }

        throw new UsernameNotFoundException("Username '" + username + "' not found !");
    }

    @Override
    public User findByUsername(String username) {

        return userRepository.findByUsername(username);
    }

    /**
     * revert user by username and password 
     * 
     * @param username
     * @param password
     * @return
     * @throws UsernameNotFoundException
     */
    @NotNull
    @Override
    public User findByUsernameAndPassword(String username, String password) throws UsernameNotFoundException {

        Optional<User> userOpt = userRepository.findByUsernameAndPassword(username, password);
        User user = null;
        
        // check if its "Insured"
        if(userOpt.isPresent()) user = userOpt.get();
        
        // check if its external system
        else {
            
            // unique user credentials for external insurance system purpose
            Optional<InMemoryUser> inMemoryUserOptional = Optional
                .ofNullable(appService.getAppProperties())
                .map(AppProperties::getSecurity)
                .map(Security::getInMemoryUser);

            if (inMemoryUserOptional.isPresent()) {
                
                InMemoryUser inMemoryUser = inMemoryUserOptional.get();
                
                String inMemoryUsername = Optional
                    .ofNullable(inMemoryUser.getUsername())
                    .orElse("");

                if (inMemoryUsername.equalsIgnoreCase(username)) {

                    String inMemoryPassword = inMemoryUser.getPassword();
                    String inMemoryRole = inMemoryUser.getRole();

                    user = new User();
                    user.setId(-1L);
                    user.setClient(null);
                    user.setUsername(inMemoryUsername);
                    user.setPassword(inMemoryPassword);
                    user.setRole(inMemoryRole);
                    
                }
            }
        }
        
        if(Objects.isNull(user))
            throw new UsernameNotFoundException(
                "User with the username '" + username +
                    "' and password '" + password + "' cannot be found !"
            );
        
        return user;

    }
    
    @Override
    public AppAuthHistory externalSignIn(User user) {

        log.info("External authentication process...");

        var credentials = new BasicCredentials(user.getUsername(), user.getPassword());
        
        validateCredentials(credentials);
        
        // get client
        Client userClient = user.getClient();

        // get api
        ExternalWsApi externalWsApi = externalWsApiRepository
            .findById(userClient.getId())
            .orElseThrow(
                () -> new IllegalArgumentException("No external WS API for client with ID: " + userClient.getId())
            );

        WsConfig wsSignInConfig = externalWsService.getExternalWsConfig(externalWsApi);

        WsSignInExternalResponse signInResponse = externalWsService.getWsSignInExternalResponse(credentials, externalWsApi, wsSignInConfig);

        externalWsService.validateWsSignInExternalResponse(signInResponse);

        AppAuthHistory authHistory = authHistoryService.updateAuthHistory(credentials, user, signInResponse);

        return authHistory;
    }
    
    @Override
    public BasicAuthResponse buildBasicAuthResponse(BasicCredentials credentials/*, String token*/) {
        
        // build this ws response
        BasicAuthResponse authResponse = new BasicAuthResponse();

        // basic authentication token
//        String[] tokenExpAsArray = token.split(" ");

        // token example: "Basic 4az84z84d8za4dzaz=="
        // after split it becomes { "Basic", "4az84z84d8za4dzaz==" }
        // here we take the second part only
//        authResponse.setToken(tokenExpAsArray[1]);
        authResponse.setMessage("Hello " + credentials.username() + " !");
        
        return authResponse;
    }

    @NotNull
    @Override
    public User getByDevice(Device device){
        
        User user = userRepository.findByDevicesContains(device)
            .orElseThrow(
                () -> new RuntimeException("The device with ID : " + device.getId() + " doesn't belong to any user")
            );

        return user;
    }
    
    // helper methods
    
    private static void validateCredentials(BasicCredentials credentials) {

        log.info("-- Validating credentials...");

        // credentials validation as strings
        boolean validCredentials = hasValidCredentials(credentials);

        if (!validCredentials)
            throw new RuntimeException("Invalid auth credentials received");

        log.info("-- Credentials are valid.");
    }

    public static boolean hasValidCredentials(BasicCredentials credentials) {

        final String USERNAME = "'username'";
        final String PASSWORD = "'password'";
        String ErrorExpression = "No %s has been received!";

        // Null check
        ExtranetUtils.validateObject(credentials.username(), USERNAME, ErrorExpression);
        ExtranetUtils.validateObject(credentials.password(), PASSWORD, ErrorExpression);

        // empty + blank check
        boolean usernameNotEmpty = StringUtils.hasText(credentials.username());
        boolean passwordNotEmpty = StringUtils.hasText(credentials.password());

        boolean validCredentials = usernameNotEmpty && passwordNotEmpty;

        return validCredentials;
    }
    
}
