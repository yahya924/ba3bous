package com.igatn.extranet.security.providers.otp;

import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.security.models.otp.OtpSenderAuthentication;
import com.igatn.extranet.service.otp.OtpService;
import com.igatn.extranet.service.user.UserServiceImpl;
import com.igatn.extranet.utils.ExtranetUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@Component
public class OtpSenderAuthenticationProvider implements AuthenticationProvider {

    private final UserServiceImpl userServiceImpl;
    
    private final OtpService otpService;

    public OtpSenderAuthenticationProvider(UserServiceImpl userServiceImpl, OtpService otpService) {
        this.userServiceImpl = userServiceImpl;
        this.otpService = otpService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        final String OTP_USERNAME = authentication.getName();
        
        if(!StringUtils.hasText(OTP_USERNAME))
            throw new SecurityException("Invalid otp username: " + OTP_USERNAME);

        if(!(authentication.getCredentials() instanceof final String OTP_AUTHORIZATION))
            throw new SecurityException("OTP-Send: Invalid OTP authorization");

        String defaultError = "Wrong OTP authorization: " + OTP_AUTHORIZATION;
        
        var OTP_TOKEN_SPLIT = ExtranetUtils.validateOtpToken(OTP_AUTHORIZATION, defaultError);

        final String USERNAME_CLAIM = OTP_TOKEN_SPLIT[0];
        
        // check if otp username refers to another user
        Optional
            .ofNullable(userServiceImpl.findByUsername(OTP_USERNAME))
            .ifPresent((suspect) ->{
                
                String suspectUsername = suspect.getUsername();
                boolean sameUser = suspectUsername.equalsIgnoreCase(USERNAME_CLAIM);
                
                if(!sameUser)
                    throw new SecurityException("Cannot send OTP code to another user!");
            });

        User user = userServiceImpl.loadUserByUsername(USERNAME_CLAIM);
        
        otpService.sendOtpCodeEmail(user, OTP_USERNAME);
        
        // we're going to reuse the authorization in reception
        return new OtpSenderAuthentication(user.getUsername());

    }



    @Override
    public boolean supports(Class<?> aClass) {
        return OtpSenderAuthentication.class.isAssignableFrom(aClass);
    }
}
