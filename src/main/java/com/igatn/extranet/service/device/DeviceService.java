package com.igatn.extranet.service.device;

import com.igatn.extranet.domainjpa.impl.domain.device.Device;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.user.models.BiometricParameters;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public interface DeviceService {
    
    void addDeviceByUser(User user, BiometricParameters.BiometricConfig params);

    void updateDeviceByUser(User user, BiometricParameters.BiometricConfig params, Device device);

    boolean isDeviceAuthorized(BiometricParameters.BiometricCredentials params);
    
    void registerOrUpdate(@NotNull User owner, @NotBlank String deviceOsId, @NotBlank String pinCode);

    @NotNull Device getByOsId(String deviceOsId);
    
    @NotNull Device getByOsIdAndPinCode(String deviceOsId, String pinCode);
}
