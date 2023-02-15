package com.igatn.extranet.service.device;

import com.igatn.extranet.domainjpa.api.data.DeviceRepository;
import com.igatn.extranet.domainjpa.impl.domain.auth.DevicePin;
import com.igatn.extranet.domainjpa.impl.domain.device.Device;
import com.igatn.extranet.domainjpa.impl.domain.user.User;
import com.igatn.extranet.rest.exceptions.OccuredErrorException;
import com.igatn.extranet.rest.user.models.BiometricParameters;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;

@Slf4j
@Service
public class DeviceServiceImpl implements DeviceService{
    
    private final DeviceRepository deviceRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public void addDeviceByUser(User user, BiometricParameters.BiometricConfig params) {
        Device newDevice = new Device();
        newDevice.setDeviceId(params.getDeviceId());
        newDevice.setOwner(user);
        newDevice.setPublicKey(params.getPublicKey());
        newDevice.setActive(params.getState());

        deviceRepository.save(newDevice);

    }

    @Override
    public void updateDeviceByUser(User user, BiometricParameters.BiometricConfig params, Device device) {
        device.setActive(params.getState());
        device.setOwner(user);
        device.setDeviceId(params.getDeviceId());
        device.setPublicKey(params.getPublicKey());

        deviceRepository.save(device);

    }

    @Override
    public boolean isDeviceAuthorized(BiometricParameters.BiometricCredentials params) {
        
        try {

            Optional<Device> deviceExists = deviceRepository.findByDeviceId(params.getDeviceId());
            
            if (deviceExists.isPresent()) {
                
                log.info("device found is " + params.getDeviceId());
                String key = deviceExists.get().getPublicKey();

                PublicKey publicKey = getPublicKey(key);
                return verifySignature(params.getSignature(), params.getPayload(), publicKey);

            }

            return false;

        } catch (Exception e) {
            throw new OccuredErrorException(e.getMessage());
        }
    }

    /**
     * register or update a device
     * 
     * @param owner
     * @param deviceOsId
     * @param pinCode
     */
    @Override
    public void registerOrUpdate(
        @NotNull User owner, 
        @NotBlank String deviceOsId, 
        @NotBlank String pinCode
    ) {
        
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceOsId);
        boolean exist = deviceOpt.isPresent();
        Device target;
        
        if (exist) {
            
            target = deviceOpt.get();
            target.reinitialize(owner, pinCode);

        } else {

            DevicePin pin = new DevicePin(pinCode);
            target = new Device(deviceOsId, owner, pin);
        }
        
        deviceRepository.save(target);
    }

    /**
     * revert by device os id
     * 
     * @param deviceOsId
     * @return
     */
    @Override
    public @NotNull Device getByOsId(String deviceOsId) {
        
        Device device = deviceRepository
            .findByDeviceId(deviceOsId)
            .orElseThrow(
            () -> new RuntimeException("No device found with 'deviceOsId': " + deviceOsId)
        );
        
        return device;
    }

    @Override
    public @NotNull Device getByOsIdAndPinCode(String deviceOsId, String pinCode) {

        Device target = deviceRepository
            .findByDeviceIdAndPinCode(deviceOsId, pinCode)
            .orElseThrow(
                () -> new RuntimeException(
                    "No device found with 'deviceOsId': " + deviceOsId +
                        "And 'pin code': " + pinCode
                )
            );

        return target;
    }

    private static PublicKey getPublicKey(String pubKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(org.apache.commons.codec.binary.Base64.decodeBase64(pubKey));
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new OccuredErrorException(e.getMessage());
        }
    }

    private static boolean verifySignature(String signature, String payload, PublicKey key) {

        try {
            Signature sha_rsa = Signature.getInstance("SHA256withRSA");
            sha_rsa.initVerify(key);
            sha_rsa.update(payload.getBytes());
            return sha_rsa.verify(Base64.decodeBase64(signature));

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new OccuredErrorException(e.getMessage());
        }
    }
}
