package com.igatn.extranet.security.providers.biometrics;

import com.igatn.extranet.domainjpa.impl.domain.device.Device;
import com.igatn.extranet.domainjpa.impl.domain.tracking.AppAuthHistory;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.user.models.BiometricParameters.BiometricCredentials;
import com.igatn.extranet.security.models.biometric.BiometricAuthentication;
import com.igatn.extranet.service.device.DeviceService;
import com.igatn.extranet.service.user.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class BiometricsAuthenticationProvider implements AuthenticationProvider {

    private final UserServiceImpl userServiceImpl;

    private final DeviceService deviceService;

    public BiometricsAuthenticationProvider(
        UserServiceImpl userServiceImpl,
        DeviceService deviceService
    ) {
        this.userServiceImpl = userServiceImpl;
        this.deviceService = deviceService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException, SecurityException {

        Object credentials = authentication.getCredentials();

        if(!(credentials instanceof BiometricCredentials biometricCredentials))
            throw new SecurityException("Wrong Pin auth credentials");

        // verify user by pin and device identity

        boolean isAuthorized = deviceService.isDeviceAuthorized(biometricCredentials);

        if(!isAuthorized)
            throw new SecurityException("Unauthorized device: " + biometricCredentials.getDeviceId());

        Device device = deviceService.getByOsId(((BiometricCredentials) credentials).getDeviceId());
        
        User user = userServiceImpl.getByDevice(device);

        // external sign in
        AppAuthHistory authHistory = userServiceImpl.externalSignIn(user);
        
        return new BiometricAuthentication(authHistory.getToken());

    }

    @Override
    public boolean supports(Class<?> aClass) {
        return BiometricAuthentication.class.isAssignableFrom(aClass);
    }
}
