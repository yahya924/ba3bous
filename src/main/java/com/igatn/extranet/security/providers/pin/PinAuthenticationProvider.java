package com.igatn.extranet.security.providers.pin;

import com.igatn.extranet.domainjpa.impl.domain.device.Device;
import com.igatn.extranet.domainjpa.impl.domain.tracking.AppAuthHistory;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.security.models.pin.PinAuthentication;
import com.igatn.extranet.security.models.pin.dto.PinAuthCredentials;
import com.igatn.extranet.service.device.DeviceService;
import com.igatn.extranet.service.user.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class PinAuthenticationProvider implements AuthenticationProvider {

    final UserServiceImpl userServiceImpl;

    final DeviceService deviceService;
    
    
    public PinAuthenticationProvider(
        UserServiceImpl userServiceImpl,
        DeviceService deviceService
    ) {
        this.userServiceImpl = userServiceImpl;
        this.deviceService = deviceService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        
        Object credentials = authentication.getCredentials();
        
        if(!(credentials instanceof PinAuthCredentials pinAuthCredentials))
            throw new SecurityException("Wrong Pin auth credentials");
        
        // verify user by pin and device identity
        
        Device device = deviceService.getByOsIdAndPinCode(
            pinAuthCredentials.deviceId(), 
            pinAuthCredentials.pinCode()
        );
        
        // validate pin param
        String pinCodeParam = Optional
            .ofNullable(pinAuthCredentials.pinCode())
            .orElseThrow(() -> new SecurityException("No 'pin code' parameter received!"));

        final boolean VALID_PIN_CODE = pinCodeParam.equals(device.getPin().getCode());

        if (!VALID_PIN_CODE)
            throw new SecurityException("Invalid pin code : " + pinCodeParam);

        String username = device.getOwner().getUsername();
        
        User user = userServiceImpl.loadUserByUsername(username);
        
        // external auth required
        AppAuthHistory authHistory = userServiceImpl.externalSignIn(user);
        
        return new PinAuthentication(authHistory.getToken());

    }

    @Override
    public boolean supports(Class<?> aClass) {
        return PinAuthentication.class.isAssignableFrom(aClass);
    }
}

