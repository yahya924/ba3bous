package com.igatn.extranet.security.providers.otp;

import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.security.models.otp.OtpReceiverAuthentication;
import com.igatn.extranet.security.models.otp.dto.OtpAuthCredentials;
import com.igatn.extranet.service.otp.OtpService;
import com.igatn.extranet.service.user.UserServiceImpl;
import com.igatn.extranet.utils.ExtranetUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class OtpReceiverAuthenticationProvider implements AuthenticationProvider {

    private final UserServiceImpl userServiceImpl;
    
    private final OtpService otpService;

    public OtpReceiverAuthenticationProvider(UserServiceImpl userServiceImpl, OtpService otpService) {
        this.userServiceImpl = userServiceImpl;
        this.otpService = otpService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        
        if(!(authentication.getCredentials() instanceof final OtpAuthCredentials otpAuthCredentials))
            throw new SecurityException("OTP-Receive: Invalid OTP authorization");

        String otpAuthorization = otpAuthCredentials.otpAuthorization();
        String otpCode = otpAuthCredentials.otpCode();
        
        String defaultError = "Wrong OTP authorization: " + otpAuthorization;

        var OTP_TOKEN_SPLIT = ExtranetUtils.validateOtpToken(otpAuthorization, defaultError);
        
        if (OTP_TOKEN_SPLIT.length != 1)
            throw new SecurityException(defaultError);
        
        var otpUsername = OTP_TOKEN_SPLIT[0];
        
        User user = userServiceImpl.loadUserByUsername(otpUsername);
        
        boolean result = otpService.checkCodeByUser(otpCode, user);

        if(!result)
            throw new RuntimeException("Invalid OTP code for user with ID: " + user.getId());
        
        return new OtpReceiverAuthentication(user.getUsername(), otpCode);

    }


    @Override
    public boolean supports(Class<?> aClass) {
        return OtpReceiverAuthentication.class.isAssignableFrom(aClass);
    }
}
