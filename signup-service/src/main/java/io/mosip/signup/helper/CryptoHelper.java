package io.mosip.signup.helper;

import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.services.CacheUtilService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
@Component
public class CryptoHelper {

    private static final String AES_TRANSFORMATION = "AES/CFB/PKCS5Padding";
    public static final String CACHE_KEY = "aes";

    @Autowired
    private CacheUtilService cacheUtilService;

    public String symmetricEncrypt(String transactionId, String data, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            byte[] initializationVector = IdentityProviderUtil.generateSalt(cipher.getBlockSize());
            byte[] secretDataBytes = data.getBytes(StandardCharsets.UTF_8);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(initializationVector));
            byte[] encryptedBytes = cipher.doFinal(secretDataBytes, 0, secretDataBytes.length);

            String keyAlias = getKeyAlias(transactionId);
            byte[] keyAliasBytes = keyAlias.getBytes();
            cacheUtilService.setSecretKeyBasedOnAlias(keyAlias, IdentityProviderUtil.b64Encode(secretKey.getEncoded()));

            byte[] output = new byte[cipher.getOutputSize(secretDataBytes.length)+cipher.getBlockSize()+keyAliasBytes.length];
            System.arraycopy(encryptedBytes, 0, output, 0, encryptedBytes.length);
            System.arraycopy(initializationVector, 0, output, encryptedBytes.length, initializationVector.length);
            System.arraycopy(keyAliasBytes, 0, output, encryptedBytes.length+initializationVector.length, keyAliasBytes.length);
            return IdentityProviderUtil.b64Encode(output);

        } catch (Exception e) {
            log.error("Error Cipher Operations for provided secret data.", e);
            throw new SignUpException("crypto_error");
        }
    }

    public String symmetricDecrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);

            byte[] data = IdentityProviderUtil.b64Decode(encryptedData);
            byte[] keyAlias = Arrays.copyOfRange(data, data.length - 10, data.length);
            byte[] iv = Arrays.copyOfRange(data, data.length-(cipher.getBlockSize()+10), data.length-10);
            byte[] encryptedBytes = Arrays.copyOfRange(data, 0, data.length-(cipher.getBlockSize()+10));

            String encodedSecretKey = cacheUtilService.getSecretKey(new String(keyAlias));
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(IdentityProviderUtil.b64Decode(encodedSecretKey), "AES"),
                    new IvParameterSpec(iv));
            return new String(cipher.doFinal(encryptedBytes, 0, encryptedBytes.length));
        } catch (Exception e) {
            log.error("Error Cipher Operations for provided secret data.", e);
            throw new SignUpException("crypto_error");
        }
    }


    public SecretKey getSecretKey() {
        String encodedSecretKey = cacheUtilService.getSecretKey();
        try {
            if(encodedSecretKey == null) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                keyGenerator.init(256);
                cacheUtilService.setSecretKey(CACHE_KEY, IdentityProviderUtil.b64Encode(keyGenerator.generateKey().getEncoded()));
                encodedSecretKey = cacheUtilService.getSecretKey();
            }
            return new SecretKeySpec(IdentityProviderUtil.b64Decode(encodedSecretKey), "AES");
        } catch (Exception e) {
            log.error("Error getting secret key", e);
            throw new SignUpException("crypto_error");
        }
    }

    private String getKeyAlias(String transactionId) {
        return transactionId.substring(transactionId.length()-10);
    }
}
