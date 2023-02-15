package com.igatn.extranet.rest.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.igatn.extranet.app.AppService;
import com.igatn.extranet.domainjpa.api.data.DeviceRepository;
import com.igatn.extranet.domainjpa.api.data.LanguageRepository;
import com.igatn.extranet.domainjpa.api.data.UserRepository;
import com.igatn.extranet.domainjpa.api.data.demand.DemandRepository;
import com.igatn.extranet.domainjpa.api.data.demand.TypeDemandRepository;
import com.igatn.extranet.domainjpa.api.data.otp.OtpCodeRepository;
import com.igatn.extranet.domainjpa.api.data.ws.ExternalWsApiRepository;
import com.igatn.extranet.domainjpa.api.data.ws.WsConfigRepository;
import com.igatn.extranet.domainjpa.impl.domain.auth.OtpCode;
import com.igatn.extranet.domainjpa.impl.domain.client.Client;
import com.igatn.extranet.domainjpa.impl.domain.demand.Demand;
import com.igatn.extranet.domainjpa.impl.domain.demand.TypeDemand;
import com.igatn.extranet.domainjpa.impl.domain.device.Device;
import com.igatn.extranet.domainjpa.impl.domain.translation.Language;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.ExternalWsApi;
import com.igatn.extranet.domainjpa.impl.domain.ws.entities.WsConfig;
import com.igatn.extranet.rest.beneficiaries.model.BeneficiariesDefinition;
import com.igatn.extranet.rest.beneficiaries.model.FormattedBeneficiaries;
import com.igatn.extranet.rest.exceptions.*;
import com.igatn.extranet.rest.exceptions.ws.ExternalWsNoResponseException;
import com.igatn.extranet.rest.models.ws.WsSignInExternalResponse;
import com.igatn.extranet.rest.user.models.*;
import com.igatn.extranet.rest.user.models.BiometricParameters.BiometricConfig;
import com.igatn.extranet.rest.user.models.BiometricParameters.BiometricCredentials;
import com.igatn.extranet.rest.user.models.MissingDocsDefinition.WsMissingDocsResponse;
import com.igatn.extranet.service.demand.DemandService;
import com.igatn.extranet.service.device.DeviceService;
import com.igatn.extranet.service.user.UserService;
import com.igatn.extranet.utils.EncryptionUtils;
import com.igatn.extranet.utils.MailUtils;
import com.igatn.extranet.utils.RestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * OJA - User Controller
 * 
 */
@Slf4j
@RestController
@RequestMapping(path = "/users", produces = "application/json")
//@CrossOrigin(origins = "*") // for CORS issues
public class UserController {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private RestUtils restUtils;

    @Autowired
    private MailUtils mailUtils;

    @Autowired
    private EncryptionUtils encryptionUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TypeDemandRepository typeDemandRepository;

    @Autowired
    private DemandRepository demandRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private OtpCodeRepository codeRepository;

    @Autowired
    private ExternalWsApiRepository externalWsApiRepository;

    @Autowired
    private WsConfigRepository wsConfigRepository;

    @Autowired
    private AppService appService;

    @Autowired
    private UserService userService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DemandService demandService;

    // FRE - FIXME - Unused attribute?  
    @Value("${igatn.extranet.security.headers.internal-encryption-key-header}")
    private String aesSharedKeyHeader;

    @Autowired
    private DeviceRepository deviceRepository;

    /**
     * test the controller
     *
     * @return
     */
    @GetMapping
    private String helloUser() {
        log.info("/users");

        return "Hello from /user";
    }

    /**
     * @deprecated
     * Please refer to Security filters
     * 
     * @param credentials
     * @return
     */
    @PostMapping("signIn")
    private ResponseEntity<BasicAuthResponse> signIn(
        @RequestBody BasicCredentials credentials, 
        @RequestHeader("Authorization") String token,
        Authentication auth
    ) {

        log.info("processing /users/singIn in progress ");

        User user = (User) auth.getPrincipal();
        // sign in user to external insurance system
        // if success push token to session
        // otherwise return response error with global exception resolver 
        userService.externalSignIn(user);

        // at this stage everything must be fine
        BasicAuthResponse authResponse = userService.buildBasicAuthResponse(credentials);

        log.info("Sending /users/signIn response to client-side...");

        return ResponseEntity.ok(authResponse);
    }

    /**
     * OJA - Return the user's details
     * 
     * @param deviceOsId
     * @return {@link ResponseEntity<>} 
     */
    @GetMapping(path = "/getDetails")
    private ResponseEntity<String> getDetails(
        // FRE - this parameter is not the identifier of the device backend. 
        // it's another identity sent from OS
        @RequestParam(value = "deviceId") String deviceOsId
    ) {

        log.info("-- /users/getDetails endpoint reached! -- \n");
        log.info("Processing query 'userDetails' to third party API... \n");

        try {

            // TODO: save this ws definition in Database (wsHost, wsConfig)
            String externalWsUrl = "http://192.168.216.17:5700/api/users/getDetails";

            log.info("Processing query 'userDetails' to third party API \n");
            log.info("Calling third party API with query: " + externalWsUrl);

            ResponseEntity<UserDetailsDefinition> responseEntity = restUtils
                .prepareGetWS(
                    externalWsUrl,
                    new HashMap<>(),
                    UserDetailsDefinition.class
                );
            
            UserDetailsDefinition userDetailsResponse = responseEntity.getBody();

            if (userDetailsResponse == null || !userDetailsResponse.getSuccess()) {

                String errorMessage = "The user details third party API is not returning a valid response.";

                if (userDetailsResponse != null) {
                    Optional<String> apiErrorMessage = Arrays.stream(userDetailsResponse.getErrorMsgs()).findFirst();

                    if (apiErrorMessage.isPresent()) {
                        errorMessage = apiErrorMessage.get();
                    }
                }

                throw new ExternalWsNoResponseException(errorMessage);
            }

            ResponseModel response = userDetailsResponse.getItem();
            
            log.info("Preparing suitable response... \n");

            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            List<String> dateParts = Arrays.asList(response.getBirthDate().split("T")[0].split("-"));

            Collections.reverse(dateParts);

            response.setSuccess(userDetailsResponse.getSuccess());
            response.setBirthDate(StringUtils.join(dateParts, "/"));
            response.setRole(user.getRole());
            response.setLanguage(user.getLanguage());
            response.setNotificationEnabled(user.getNotificationConfig().isActive());

            Device userDevice = deviceRepository
                .findByOwnerAndDeviceId(user, deviceOsId)
                .orElseThrow(
                    () -> new IllegalArgumentException(
                        "Device with 'deviceOsId' " + deviceOsId +
                        " is not found for user with ID: " + user.getId()
                    )
                );

            response.setBiometricEnabled(userDevice.isActive());

            ObjectWriter objectWriter = new ObjectMapper().writer()
                .withDefaultPrettyPrinter();

            String jsonResponse = objectWriter.writeValueAsString(response);

            return ResponseEntity.ok(jsonResponse);
        } catch (Exception e) {
            String errorMessage = "The user details third party API is not returning a valid response.";
            throw new ExternalWsNoResponseException(errorMessage);
        }
    }

    /**
     *  ASGHIR - get the user beneficiaries
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping(path = "/getBeneficiaries")
    private ResponseEntity<?> getBeneficiaries(Authentication auth) throws JsonProcessingException {

        log.info("-- /users/getBeneficiaries endpoint reached! -- \n");
        log.info("Processing query 'Beneficiaries' to third party API... \n");

        String url = "http://192.168.216.17:5700/api/users/getBeneficiaries";

        log.info("Processing query 'Beneficiaries' to third party API finished! \n");
        log.info("Calling third party API with query: " + url);

        ResponseEntity<BeneficiariesDefinition> responseBeneficiaries = restUtils.prepareGetWS(url, new HashMap<>(), BeneficiariesDefinition.class);
        log.info("Ws beneficiaries response: " + responseBeneficiaries);

        Optional<BeneficiariesDefinition> responseBody = Optional.ofNullable(responseBeneficiaries.getBody());
        BeneficiariesDefinition response = responseBody.orElseThrow(
            ()-> new ExternalWsNoResponseException("WS /getBeneficiaries/getAll is not returning a valid response")
        );

        FormattedBeneficiaries formattedBeneficiaries = new FormattedBeneficiaries();
        formattedBeneficiaries.setSuccess(response.getSuccess());
        formattedBeneficiaries.setBeneficiaries(response.getItem());

        ApiError apiError = new ApiError();
        if (Boolean.FALSE.equals(response.getSuccess())) {
            apiError.setCode(500);
            apiError.setMessage(Arrays.stream(response.getErrorMsgs()).findFirst().get());

            return new ResponseEntity<>(apiError,null, apiError.getCode());
        }

        log.info("response ready to go: \n"+ responseBeneficiaries);

        return ResponseEntity.ok(formattedBeneficiaries);
    }
    
    /**
     * OJA - Update the user's password
     *
     * @return
     */
    @PostMapping(path = "/updatePassword")
    private ResponseEntity<GenericSuccessResponse> updatePassword(@RequestBody UpdatePasswordParameters newCredentials) {
        log.info("processing /users/updatePassword in progress.");

        String username = newCredentials.getUsername();
        String oldPassword = newCredentials.getOldPassword();
        String newPassword = newCredentials.getNewPassword();

        if (username != null && oldPassword != null && newPassword != null) {
            Optional<User> matchingUserToGet = userRepository.findByUsernameAndPassword(username, oldPassword);

            if (matchingUserToGet.isPresent()) {
                User user = matchingUserToGet.get();

                Client userClient = user.getClient();

                Optional<ExternalWsApi> clientExternalHost = externalWsApiRepository.findById(userClient.getId());

                if (clientExternalHost.isPresent()) {
                    ExternalWsApi clientExternalHostCore = clientExternalHost.get();

                    Optional<WsConfig> wsUpdatePasswordConfig = wsConfigRepository.findByHostAndLabel(clientExternalHostCore, "Insured update password");

                    if (wsUpdatePasswordConfig.isPresent()) {
                        WsConfig wsUpdatePasswordConfigCore = wsUpdatePasswordConfig.get();

                        String url = clientExternalHostCore.getProtocol() + "://" + clientExternalHostCore.getHostname() + ':' +
                                clientExternalHostCore.getPort() + clientExternalHostCore.getRootPath() + wsUpdatePasswordConfigCore.getPath();

                        ApiBasicCredentials basicCredentials = new ApiBasicCredentials(username, newPassword);

                        ResponseEntity<WsSignInExternalResponse> sagileaApiResponse = restUtils.preparePostWS(url, new HashMap<>(), WsSignInExternalResponse.class,
                                basicCredentials);

                        WsSignInExternalResponse response = sagileaApiResponse.getBody();

                        if (response == null || !response.isSuccess()) {
                            String errorMessage = "The external API call is not returning a valid response";

                            if (response != null) {
                                Optional<String> apiErrorMessage = Arrays.stream(response.getErrorMsgs()).findFirst();

                                if (apiErrorMessage.isPresent()) {
                                    errorMessage = apiErrorMessage.get();
                                }
                            }

                            throw new ExternalWsNoResponseException(errorMessage);
                        }

                        log.info("replacing password in database.");

                        user.setPassword(newPassword);
                        userRepository.save(user);

                        TypeDemand updatePasswordTypeDemand = typeDemandRepository.findByCode("PWD");

                        if (updatePasswordTypeDemand != null) {
                            Demand demand = new Demand();
                            demand.setType(updatePasswordTypeDemand);
                            demand.setSender(user);
                            demand.setMessage("");
                            demand.setSubject("Mise à jour du mot de passe");
                            demandRepository.save(demand);

                            GenericSuccessResponse successResponse = new GenericSuccessResponse(true);

                            return ResponseEntity.ok(successResponse);
                        }

                        throw new ReferentialValueNotFoundException();
                    }

                    throw new WsExternalApiNotFoundException("Insured update password external API was not found in the database.");
                }

                throw new ClientNotFoundException("The external host can't be found in the database!");
            }

            throw new NoUserFoundByUsernameProvidedException();
        }

        throw new InvalidWsParametersException();
    }

    /**
     * OJA - Update the user's forgotten password
     *
     * @return
     */
    @PostMapping(path = "/forgetPassword/update")
    private ResponseEntity<GenericSuccessResponse> updateForgottenPassword(@RequestBody UpdateForgottenPasswordParameters newCredentials) {
        log.info("processing /users/forgetPassword/update in progress.");

        String email = newCredentials.getEmail();
        String password = newCredentials.getPassword();

        if (email != null && password != null) {
            User user = userRepository.findByUsername(email);

            if (user != null) {
                Client userClient = user.getClient();

                Optional<ExternalWsApi> clientExternalHost = externalWsApiRepository.findById(userClient.getId());

                if (clientExternalHost.isPresent()) {
                    ExternalWsApi clientExternalHostCore = clientExternalHost.get();

                    Optional<WsConfig> wsUpdateForgottenPasswordConfig = wsConfigRepository.findByHostAndLabel(clientExternalHostCore,
                            "Insured forget password update");

                    if (wsUpdateForgottenPasswordConfig.isPresent()) {
                        WsConfig wsUpdateForgottenPasswordConfigCore = wsUpdateForgottenPasswordConfig.get();

                        String url = clientExternalHostCore.getProtocol() + "://" + clientExternalHostCore.getHostname() + ':' +
                                clientExternalHostCore.getPort() + clientExternalHostCore.getRootPath() + wsUpdateForgottenPasswordConfigCore.getPath();

                        Map<String, String> headers = new HashMap<>();

                        String noInterceptorHeaderAttribute = appService.getAppProperties().getSecurity().getHeaders().getNoInterceptorHeader();

                        headers.put(noInterceptorHeaderAttribute, "*");

                        if (wsUpdateForgottenPasswordConfigCore.getMethod() == HttpMethod.POST) {
//                            SecretKey aesGeneratedKey = encryptionUtils.getAESInternalKey();

                            String aesSharedKey = appService.getAppProperties().getSecurity().getKeys().getAesEncryptionSharedKey();

                            SecretKey aesSharedSecretKey = encryptionUtils.getSecretKeyFromString(aesSharedKey);

                            String requestBody = encryptionUtils.encryptString(newCredentials.toString(), aesSharedSecretKey);

//                            headers.put(aesSharedKeyHeader, encryptionUtils.encryptKey(aesGeneratedKey, aesSharedSecretKey));

                            ResponseEntity<WsSignInExternalResponse> sagileaApiResponse = restUtils.preparePostWS(url, new HashMap<>(),
                                    WsSignInExternalResponse.class, requestBody, headers);

                            WsSignInExternalResponse response = sagileaApiResponse.getBody();

                            if (response == null || !response.isSuccess()) {
                                String errorMessage = "The external API call is not returning a valid response";

                                if (response != null) {
                                    Optional<String> apiErrorMessage = Arrays.stream(response.getErrorMsgs()).findFirst();

                                    if (apiErrorMessage.isPresent()) {
                                        errorMessage = apiErrorMessage.get();
                                    }
                                }

                                throw new ExternalWsNoResponseException(errorMessage);
                            }

                            log.info("replacing password in database.");

                            user.setPassword(password);
                            userRepository.save(user);

                            TypeDemand updatePasswordTypeDemand = typeDemandRepository.findByCode("PWD");

                            if (updatePasswordTypeDemand != null) {
                                Demand demand = new Demand();
                                demand.setType(updatePasswordTypeDemand);
                                demand.setSender(user);
                                demand.setMessage("");
                                demand.setSubject("Mise à jour du mot de passe");
                                demandRepository.save(demand);

                                GenericSuccessResponse successResponse = new GenericSuccessResponse(true);

                                return ResponseEntity.ok(successResponse);
                            }

                            throw new ReferentialValueNotFoundException();
                        }

                        throw new InvalidHttpMethodException();
                    }

                    throw new WsExternalApiNotFoundException("Sign in external API was not found in the database.");
                }

                throw new ClientNotFoundException("The external host can't be found in the database!");
            }

            throw new NoUserFoundByUsernameProvidedException();
        }

        throw new InvalidWsParametersException();
    }

    /**
     * OJA - Update the user's app language
     *
     * @return
     */
    @PostMapping(path = "/updateLanguage")
    private ResponseEntity<GenericSuccessResponse> updateLanguage(@RequestBody UpdateLanguageParameters newLanguage, Authentication auth) {
        log.info("processing /users/updateLanguage in progress.");

        Optional<Language> language = languageRepository.findById(newLanguage.getLanguage());

        User user = (User) auth.getPrincipal();

        user.setLanguage(language.get());

        userRepository.save(user);

        GenericSuccessResponse successResponse = new GenericSuccessResponse(true);

        return ResponseEntity.ok(successResponse);
    }

    /**
     * OJA - Send code to user for account retrieval
     *
     * @return
     */
    @PostMapping(path = "/sendCode")
    private ResponseEntity<GenericSuccessResponse> sendCode(@RequestBody SendCodeParameters parameters) {
        log.info("processing /users/sendCode in progress.");

        String email = parameters.getEmail();

        if (email != null) {
            
            User user = userRepository.findByUsername(email);

            if (user != null) {

                int forgetPasswordExpirationPeriod = appService.getAppProperties().getSecurity().getExpiration().getForgetPasswordPeriod();
                String securityCode = RandomStringUtils.randomNumeric(4);

                OtpCode userCode = new OtpCode();
                userCode.setCodeValue(securityCode);
                userCode.setUser(user);
                userCode.setExpiresAt(Instant.now().plus(forgetPasswordExpirationPeriod, ChronoUnit.MILLIS));

                codeRepository.save(userCode);

                try {
                    InternetAddress[] recipients = new InternetAddress[]{new InternetAddress(email)};
                    String subject = new String("Extranet Mobile - Mot de passe oublié");
                    String content = new String("Voici le code de sécurité que vous devez utiliser pour définir votre nouveau mot de passe: " +
                            securityCode);

                    MimeMessagePreparator preparator = mailUtils.prepareMail(recipients, subject, content, null);

                    this.javaMailSender.send(preparator);
                    
                } catch (AddressException e) {
                    throw new InvalidInternetAddressException();
                }

                GenericSuccessResponse successResponse = new GenericSuccessResponse(true);

                return ResponseEntity.ok(successResponse);
            }

            throw new NoUserFoundByUsernameProvidedException();
        }

        throw new InvalidWsParametersException();
    }

    /**
     * OJA - Verify user's code for account retrieval
     *
     * @return
     */
    @PostMapping(path = "/verifyCode")
    private ResponseEntity<GenericSuccessResponse> verifyCode(@RequestBody VerifyCodeParameters parameters) {
        log.info("processing /users/verifyCode in progress.");

        String email = parameters.getEmail();
        String code = parameters.getCode();

        if (email != null || code != null) {
            User user = userRepository.findByUsername(email);

            if (user != null) {
                OtpCode lastUserCode = codeRepository.findTopByUserOrderByCreatedAtDesc(user);

                if (lastUserCode != null &&
                        Objects.equals(lastUserCode.getCodeValue(), code) &&
                        Instant.now().isBefore(lastUserCode.getExpiresAt())) {
                    lastUserCode.setMatchedAt(Instant.now());

                    codeRepository.save(lastUserCode);

                    GenericSuccessResponse successResponse = new GenericSuccessResponse(true);

                    return ResponseEntity.ok(successResponse);
                }

                throw new UserNoCodeMatcherException();
            }

            throw new NoUserFoundByUsernameProvidedException();
        }

        throw new InvalidWsParametersException();
    }


    @GetMapping(path = "/missingDocs/getAll")
    private ResponseEntity<WsMissingDocsResponse> getMissingDocs(
            Authentication authentication,
            @RequestParam(value = "language", required = true, defaultValue = "fr") String languageIsoCode,
            @RequestParam(value = "skip", required = true, defaultValue = "0") Integer indexFrom,
            @RequestParam(value = "take", required = true, defaultValue = "5") Integer indexTo,
            @RequestParam(value = "type", required = true) String type,
            @RequestParam(value = "status", required = false) String statusCode
    ) {
        log.info("-- /users/getMissingDocs/getAll endpoint called! --");
        try {

            MissingDocsListParams params = MissingDocsListParams.builder()
                    .language(languageIsoCode)
                    .indexFrom(indexFrom)
                    .indexTo(indexTo)
                    .type(type)
                    .status(statusCode)
                    .build();

            User user = (User) authentication.getPrincipal();

            WsMissingDocsResponse wsListMissingDocsResponse = demandService.getAll(params, user);
            ResponseEntity<WsMissingDocsResponse> missingDocsResponse = new ResponseEntity<>(wsListMissingDocsResponse, HttpStatus.OK);
            Optional<WsMissingDocsResponse> responseBody = Optional.ofNullable(missingDocsResponse.getBody());

            WsMissingDocsResponse response = responseBody.orElseThrow(
                    () -> new ExternalWsNoResponseException("WS /users/getMissingDocs/getAll is not returning a valid response... response is NullPointer")
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new OccuredErrorException(e.getMessage());
        }
    }

    @PostMapping(path="/toggleBiometric")
    public ResponseEntity<GenericSuccessResponse> bindDeviceToUSer(
        @RequestBody BiometricConfig parameters, 
        Authentication auth
    ){
        log.info("processing /users/toggleBiometric in progress.");

        User user = (User) auth.getPrincipal();

        Optional<Device> device = deviceRepository.findByOwnerAndDeviceId(user, parameters.getDeviceId());
       
        if (device.isEmpty()) {
            
            Optional<Device> deviceExist = deviceRepository.findByDeviceId(parameters.getDeviceId());
            
            log.info(String.format("Adding new device to the current user %s", user.getUsername()));
            
            if(deviceExist.isEmpty()){
                deviceService.addDeviceByUser(user, parameters);
            } else {
                Device deviceFound = deviceExist.get();
                deviceService.updateDeviceByUser(user, parameters,deviceFound);
            }
        } else {
            log.info("Updating the state of biometric authentication");
            deviceService.updateDeviceByUser(user, parameters, device.get());
        }
        log.info("formatting WS response...");

        GenericSuccessResponse successResponse = new GenericSuccessResponse(true);

        return ResponseEntity.ok(successResponse);
    }

    /**
     * @deprecated - the following endpoint is not used
     * anymore we consider using security filters instead
     * 
     * @param credentials
     * @return
     */
    @PostMapping(path="/biometricUserAuth")
    public ResponseEntity<BasicAuthResponse> biometricLogin(
        @RequestBody BiometricCredentials credentials) {

        log.info("processing /users/biometricUserAuth in progress.");
        
        boolean isAuthorized = deviceService.isDeviceAuthorized(credentials);

        if(isAuthorized) {
            
            User user = deviceRepository.findUserByDeviceId(credentials.getDeviceId());

//            BasicAuthResponse response = userService.authenticateUser(user);

//            return ResponseEntity.ok(response);

        }
        
        throw new UnauthorizedDeviceException();

    }
}

