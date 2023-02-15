package com.igatn.extranet.utils;

import com.igatn.extranet.rest.exceptions.AESKeyGenerationException;
import com.igatn.extranet.rest.exceptions.RequestDataEncryptionException;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class EncryptionUtils {

    public SecretKey getAESInternalKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);

            return keyGenerator.generateKey();
        }
        catch (NoSuchAlgorithmException e) {
            throw new AESKeyGenerationException(e.getMessage());
        }
    }

    public SecretKey getSecretKeyFromString(String key) {
        byte[] keyToBytes = key.getBytes();

        return new SecretKeySpec(keyToBytes, 0, keyToBytes.length, "AES");
    }

    public String encryptString(String input, SecretKey key)  {
        try {
            final Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cipherText = cipher.doFinal(input.getBytes());

            return Base64.getEncoder().encodeToString(cipherText);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RequestDataEncryptionException(e.getMessage());
        }
    }

    public byte[] encryptKey(SecretKey keyToWrap, SecretKey keyToEncryptWith) {
        try {
            final Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.WRAP_MODE, keyToEncryptWith);
            return cipher.wrap(keyToWrap);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new RequestDataEncryptionException(e.getMessage());
        }
    }
}
