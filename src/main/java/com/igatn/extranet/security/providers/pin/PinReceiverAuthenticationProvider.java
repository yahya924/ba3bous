package com.igatn.extranet.security.providers.pin;

import com.igatn.extranet.domainjpa.impl.domain.auth.OtpCode;
import com.igatn.extranet.domainjpa.impl.domain.tracking.AppAuthHistory;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.security.filters.SecurityFilterConstants;
import com.igatn.extranet.security.models.pin.PinReceiverAuthentication;
import com.igatn.extranet.security.models.pin.dto.PinReceiverAuthCredentials;
import com.igatn.extranet.service.device.DeviceService;
import com.igatn.extranet.service.otp.OtpService;
import com.igatn.extranet.service.user.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;


@Slf4j
@Component
public class PinReceiverAuthenticationProvider implements AuthenticationProvider {

    private final UserServiceImpl userServiceImpl;

    private final OtpService otpService;

    private final DeviceService deviceService;
    
    public PinReceiverAuthenticationProvider(UserServiceImpl userServiceImpl, OtpService otpService, DeviceService deviceService) {
        this.userServiceImpl = userServiceImpl;
        this.otpService = otpService;
        this.deviceService = deviceService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        
        String username = authentication.getName();
        Object credentials = authentication.getCredentials();
        
        // 1 - validate pin token
        if(!(credentials instanceof PinReceiverAuthCredentials pinCredentials))
            throw new SecurityException("Wrong PIN auth credentials: " + credentials.toString());

        final String PIN_AUTHORIZATION = pinCredentials.pinAuthorization();

        final byte[] DECODED_AUTHORIZATION = Base64
            .getDecoder()
            .decode(PIN_AUTHORIZATION);

        final String TOKEN = new String(DECODED_AUTHORIZATION);

        String defaultError = "Wrong PIN authorization: " + TOKEN;

        if (!TOKEN.contains(SecurityFilterConstants.SEPARATOR)) throw new SecurityException(defaultError);

        final String[] PIN_TOKEN_SPLIT = TOKEN.split(SecurityFilterConstants.SEPARATOR);

        if (PIN_TOKEN_SPLIT.length != 3) throw new SecurityException(defaultError);
        
        //  find user
        User user = userServiceImpl.loadUserByUsername(username);
        OtpCode targetOtp = otpService.getLatestUsedByUser(user);

        final String OTP_CODE_CLAIM = PIN_TOKEN_SPLIT[1];
        final String EXPIRATION_DATE_CLAIM = PIN_TOKEN_SPLIT[2];

        final LocalDateTime EXPIRATION_DATE_TIME = LocalDateTime.parse(
            EXPIRATION_DATE_CLAIM,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        if(!LocalDateTime.now().isBefore(EXPIRATION_DATE_TIME))
            throw new SecurityException("OTP token expired!");
        
        final boolean EXACT_VALUE = targetOtp
            .getCodeValue()
            .equals(OTP_CODE_CLAIM);
        
        final boolean IS_USED = targetOtp.isUsed();
        final boolean IS_VALID = EXACT_VALUE && IS_USED;
        
        if(!IS_VALID)
            throw new SecurityException(
                "Invalid OTP code received from OTP authorization: " + PIN_AUTHORIZATION
            );

        // 2- register pin and device
        // validation
        String validDeviceId = Optional
            .ofNullable(pinCredentials.deviceId())
            .orElseThrow(() -> new SecurityException("Pin Service: No 'deviceOsId' received!"));

        String validPinCode = Optional
            .ofNullable(pinCredentials.pinCode())
            .orElseThrow(() -> new SecurityException("Pin Service: No 'Pin code' received!"));
        
        deviceService.registerOrUpdate(user, validDeviceId, validPinCode);
        
        // create
        // external auth required
        AppAuthHistory authHistory = userServiceImpl.externalSignIn(user);
        
        return new PinReceiverAuthentication(authHistory.getToken());

    }

    @Override
    public boolean supports(Class<?> aClass) {
        return PinReceiverAuthentication.class.isAssignableFrom(aClass);
    }
}

