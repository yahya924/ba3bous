package com.igatn.extranet.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.igatn.extranet.rest.exceptions.ApiError;
import com.igatn.extranet.rest.models.ws.WsInternalResponse;
import com.igatn.extranet.rest.models.ws.WsResponse;
import com.igatn.extranet.rest.user.models.AuthStep;
import com.igatn.extranet.rest.user.models.AuthSubStep;
import com.igatn.extranet.rest.user.models.BasicAuthResponse;
import com.igatn.extranet.rest.user.models.BiometricParameters.BiometricCredentials;
import com.igatn.extranet.security.models.UsernamePasswordAuthentication;
import com.igatn.extranet.security.models.biometric.BiometricAuthentication;
import com.igatn.extranet.security.models.otp.OtpReceiverAuthentication;
import com.igatn.extranet.security.models.otp.OtpSenderAuthentication;
import com.igatn.extranet.security.models.otp.dto.OtpAuthCredentials;
import com.igatn.extranet.security.models.pin.PinAuthentication;
import com.igatn.extranet.security.models.pin.PinReceiverAuthentication;
import com.igatn.extranet.security.models.pin.dto.PinAuthCredentials;
import com.igatn.extranet.security.models.pin.dto.PinReceiverAuthCredentials;
import com.igatn.extranet.service.otp.OtpType;
import com.igatn.extranet.utils.ExtranetUtils;
import io.micrometer.core.lang.NonNullApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@NonNullApi
@Component
public class InitAuthenticationFilter extends OncePerRequestFilter {

    @Value("${igatn.extranet.security.keys.signing}")
    private String signingKey;

    @Value("${igatn.extranet.security.expiration.otp-token}")
    private int otpTokenExpirationInSeconds;
    
    @Value("${igatn.extranet.security.expiration.jwt-token}")
    private int jwtExpirationInSeconds;

    private final AuthenticationManager authenticationManager;
    
    public InitAuthenticationFilter(@Lazy AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws IOException {
        
        // otp
        final String OTP_AUTHORIZATION = request.getHeader(SecurityFilterConstants.CUSTOM_HEADER_OTP_AUTHORIZATION);
        final String PARAM_OTP_TYPE = request.getHeader(SecurityFilterConstants.CUSTOM_HEADER_OTP_TYPE);
        final String PARAM_OTP_CODE = request.getHeader(SecurityFilterConstants.CUSTOM_HEADER_OTP_CODE);
        
        // PIN
        final String PIN_AUTHORIZATION = request.getHeader(SecurityFilterConstants.CUSTOM_HEADER_PIN_AUTHORIZATION);
        final String PARAM_PIN_CODE = request.getHeader(SecurityFilterConstants.CUSTOM_HEADER_PIN_CODE);
        final String PARAM_DEVICE_IDENTITY = request.getHeader(SecurityFilterConstants.CUSTOM_HEADER_DEVICE_ID);
        
        // biometrics
        final String PARAM_SIGNATURE = request.getHeader(SecurityFilterConstants.CUSTOM_HEADER_SIGNATURE);
        final String PARAM_PAYLOAD = request.getHeader(SecurityFilterConstants.CUSTOM_HEADER_PAYLOAD);

        ObjectMapper objectMapper = new ObjectMapper();

        final String PARAM_USERNAME = request.getHeader(SecurityFilterConstants.PARAMETER_USERNAME);
        final String PARAM_PASSWORD = request.getHeader(SecurityFilterConstants.PARAMETER_PASSWORD);

        // some required objects before starting //
        final ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
        response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        
//        HttpStatus errorStatus = null;
//        String errorMsg = null;
        
        // it's also possible to get parameters from post body
        // 
//        if (ObjectUtils.allNull(password, username)) {
//
//            try {
//                
//                BasicCredentials params = objectMapper.readValue(
//                    request.getInputStream(),
//                    BasicCredentials.class
//                );
//                
//                username = params.username();
//                password = params.password();
//
//                // it might be better to use try..catch here
//                // for parsing errors instead of using general catch bloc below
//
//            } catch (IOException e) {
//
//                errorMsg = "Basic Auth: Something went wrong while reading parameters from post body";
//                errorStatus = HttpStatus.INTERNAL_SERVER_ERROR;
//            }
//        }

        final boolean HAS_BASIC_CREDENTIALS = !ExtranetUtils.hasAnyNullOrEmpty(
            PARAM_USERNAME, PARAM_PASSWORD
        );
        final boolean HAS_OTP_CREDENTIALS = !ExtranetUtils.hasAnyNullOrEmpty(
            PARAM_USERNAME, PARAM_OTP_TYPE, OTP_AUTHORIZATION
        );
        final boolean HAS_OTP_CODE = !ExtranetUtils.hasAnyNullOrEmpty(
            OTP_AUTHORIZATION, PARAM_OTP_CODE
        );
        final boolean HAS_PIN_CREDENTIALS = !ExtranetUtils.hasAnyNullOrEmpty(
            PARAM_USERNAME, PARAM_PIN_CODE, PARAM_DEVICE_IDENTITY, PIN_AUTHORIZATION
        );
        final boolean HAS_PIN_CODE = !ExtranetUtils.hasAnyNullOrEmpty(
            PARAM_PIN_CODE, PARAM_DEVICE_IDENTITY
        );
        final boolean HAS_BIOMETRICS = !ExtranetUtils.hasAnyNullOrEmpty(
            PARAM_DEVICE_IDENTITY, PARAM_SIGNATURE, PARAM_PAYLOAD
        );

        final boolean IS_USING_POST_METHOD = request.getMethod()
            .equalsIgnoreCase(HttpMethod.POST.toString());

        if (!IS_USING_POST_METHOD)
            throw new SecurityException(
                "HTTP method: " + request.getMethod() + " is not allowed for authentication"
            );

        try {
            // -- basic authentication
            // do basic auth 
            if (HAS_BASIC_CREDENTIALS) {
                
                Authentication basicAuth = new UsernamePasswordAuthentication(PARAM_USERNAME, PARAM_PASSWORD);

                // do authentication
                Authentication validAuth = authenticationManager.authenticate(basicAuth);

                Collection<? extends GrantedAuthority> authorities = validAuth.getAuthorities();
                
                if(authorities.isEmpty())
                    throw new SecurityException("User has no authorities!");

                Optional<? extends GrantedAuthority> grantedAuthorityOpt = authorities
                    .stream()
                    .findFirst();

                if(grantedAuthorityOpt.isEmpty())
                    throw new SecurityException("User has no authorities!");

                else {

                    GrantedAuthority grantedAuthority = grantedAuthorityOpt.get();

                    String authority = grantedAuthority.getAuthority();

                    final String DEFAULT_MSG = "Basic authentication succeeded!";

                    if (authority.equalsIgnoreCase(SecurityFilterConstants.AUTHORITY_INSURED)) {

                        BasicAuthResponse firstHelloMsg = new BasicAuthResponse(
                            DEFAULT_MSG,
                            AuthStep.OTP,
                            AuthSubStep.OTP_SEND
                        );

                        buildHttpResponse(firstHelloMsg, HttpStatus.ACCEPTED, objectWriter, response);

                        response.addHeader(
                            SecurityFilterConstants.CUSTOM_HEADER_OTP_AUTHORIZATION,
                            // we only need the username here
                            ExtranetUtils.buildCustomAuthToken(PARAM_USERNAME,null, 0)
                        );
                        
                    } else if (authority.equalsIgnoreCase(SecurityFilterConstants.AUTHORITY_INSURANCE_SYSTEM)) {

                        WsResponse firstHelloMsg = new WsInternalResponse(DEFAULT_MSG);

                        buildHttpResponse(firstHelloMsg, HttpStatus.OK, objectWriter, response);

                        response.addHeader(
                            HttpHeaders.AUTHORIZATION,
                            // we only need the username here
                            ExtranetUtils.buildApiSecurityToken(
                                PARAM_USERNAME,
                                signingKey,
                                28_800
                            )
                        );
                    }
                }
            }
            // -- otp send
            // receive otp target and send otp code
            else if (HAS_OTP_CREDENTIALS) {

                boolean isEmail = OtpType.EMAIL.toString().equalsIgnoreCase(PARAM_OTP_TYPE);

                // currently, we only support email otp
                if (!isEmail)
                    throw new SecurityException(
                        "OTP type not supported: " + PARAM_OTP_TYPE
                    );

                // otp authorization includes: username and password
                // it's mandatory to know identify if the user already exist
                Authentication otpAuthentication = new OtpSenderAuthentication(PARAM_USERNAME, OTP_AUTHORIZATION);

                Authentication validOtpAuth = authenticationManager.authenticate(otpAuthentication);

                BasicAuthResponse authResponse = new BasicAuthResponse(
                    "OTP code sent successfully",
                    AuthStep.OTP,
                    AuthSubStep.OTP_RECEIVE
                );
                
                buildHttpResponse(authResponse, HttpStatus.ACCEPTED, objectWriter, response);

                // resend otp authorization for next step "receive"
                response.addHeader(
                    SecurityFilterConstants.CUSTOM_HEADER_OTP_AUTHORIZATION,
                    // credential here means the otp-authorization
                    ExtranetUtils.buildCustomAuthToken(
                        validOtpAuth.getName(),
                        null,
                        0
                    )
                );
            }
            // -- otp receive
            // validate otp code and return otp authorization
            else if (HAS_OTP_CODE) {

                Authentication otpAuth = new OtpReceiverAuthentication(
                    new OtpAuthCredentials(OTP_AUTHORIZATION, PARAM_OTP_CODE)
                );
                
                authenticationManager.authenticate(otpAuth);

                BasicAuthResponse authResponse = new BasicAuthResponse(
                    "OTP code received successfully",
                    AuthStep.PIN,
                    AuthSubStep.PIN_REGISTER
                );
                
                buildHttpResponse(authResponse, HttpStatus.ACCEPTED, objectWriter, response);

                response.addHeader(
                    SecurityFilterConstants.CUSTOM_HEADER_PIN_AUTHORIZATION,
                    ExtranetUtils.buildCustomAuthToken(PARAM_USERNAME, PARAM_OTP_CODE, otpTokenExpirationInSeconds)
                );
            }
            // -- pin registration
            // receive PIN code with device identity
            else if (HAS_PIN_CREDENTIALS){
                
                // build pin credentials object
                Authentication pinReceiveAuth = new PinReceiverAuthentication(
                    PARAM_USERNAME, 
                    new PinReceiverAuthCredentials(
                        PIN_AUTHORIZATION, 
                        PARAM_DEVICE_IDENTITY,
                        PARAM_PIN_CODE
                    )
                );

                // authenticate
                Authentication validAuth = authenticationManager.authenticate(pinReceiveAuth);

                if(!(validAuth.getPrincipal() instanceof String secureToken))
                    throw new SecurityException(
                        "Pin Register: Wrong authentication principal type, token must be String!"
                    );
                
                // build response
                response.setHeader(HttpHeaders.AUTHORIZATION, secureToken);
                
                BasicAuthResponse authResponse = new BasicAuthResponse(
                    SecurityFilterConstants.MSG_AUTHENTICATION_SUCCEEDED,
                    AuthStep.AUTHENTICATED,
                    AuthSubStep.PIN_AUTHENTICATION
                );
                
                buildHttpResponse(authResponse, HttpStatus.OK, objectWriter, response);
                
            }
            // -- pin authentication
            // authenticate with pin code and device id
            else if(HAS_PIN_CODE){
                
                Authentication pinAuth = new PinAuthentication(
                    new PinAuthCredentials(
                        PARAM_DEVICE_IDENTITY,
                        PARAM_PIN_CODE
                    )
                );

                // authenticate with pin
                Authentication validAuth = authenticationManager.authenticate(pinAuth);

                if(!(validAuth.getPrincipal() instanceof String secureToken))
                    throw new SecurityException(
                        "Pin Auth: Wrong authentication principal type, token must be String!"
                    );
                
                // build response
                response.setHeader(HttpHeaders.AUTHORIZATION, secureToken);

                BasicAuthResponse authResponse = new BasicAuthResponse(
                    SecurityFilterConstants.MSG_AUTHENTICATION_SUCCEEDED,
                    AuthStep.AUTHENTICATED,
                    null
                );
                
                buildHttpResponse(authResponse, HttpStatus.OK, objectWriter, response);
                
            }
            // -- biometric authentication
            // Warn: this sort of login must become available 
            // once the user did his pin registration
            // and once his activates biometric authentication
            else if(HAS_BIOMETRICS){

                Authentication biometricAuth = new BiometricAuthentication(
                    new BiometricCredentials(
                        PARAM_SIGNATURE, PARAM_DEVICE_IDENTITY, PARAM_PAYLOAD
                    )
                );
                
                var validAuth = authenticationManager.authenticate(biometricAuth);

                if(!(validAuth.getPrincipal() instanceof String secureToken))
                    throw new SecurityException(
                        "Biometric Auth: Wrong authentication principal type, token must be String!"
                    );
                
//                String secureToken = ExtranetUtils.buildApiSecurityToken(
//                    validAuth.getName(), signingKey, jwtExpirationInSeconds
//                );

                response.setHeader(HttpHeaders.AUTHORIZATION, secureToken);

                BasicAuthResponse authResponse = new BasicAuthResponse(
                    SecurityFilterConstants.MSG_AUTHENTICATION_SUCCEEDED,
                    AuthStep.AUTHENTICATED,
                    null
                );
                
                buildHttpResponse(authResponse, HttpStatus.OK,objectWriter, response);

            }
            // otherwise, return error
            else {

                HttpStatus errorStatus = HttpStatus.UNAUTHORIZED;
                final String errorMsg = "Not allowed to access: invalid parameters!";
                
                log.error(errorMsg);

                // send error
                buildAndPrintErrorMessage(response, errorStatus, objectWriter, errorMsg);
            }
            

        } catch (Exception exception) {

            String errorMsg = exception.getLocalizedMessage();
            HttpStatus errorStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            
            // send error
            buildAndPrintErrorMessage(response, errorStatus, objectWriter, errorMsg);

        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath().equals("/users/signIn");
    }

    // helpers
    private static void buildHttpResponse(
        Object responseObject,
        HttpStatus status,
        ObjectWriter writer,
        HttpServletResponse response
    ) throws IOException {
        
        ResponseEntity<Object> responseEntity = ResponseEntity.ok(responseObject);

        String responseJson = writer
            .withDefaultPrettyPrinter()
            .writeValueAsString(responseEntity.getBody());

        response.setStatus(status.value());
        response.getWriter().write(responseJson);
    }

    private static void buildAndPrintErrorMessage(
        HttpServletResponse response,
        HttpStatus status,
        ObjectWriter objectWriter,
        final String ERROR_MSG
    ) throws IOException {
        
        log.error(ERROR_MSG);

        ApiError apiError = new ApiError(status.value(), ERROR_MSG);

        buildHttpResponse(apiError, status, objectWriter, response);
    }
    
}