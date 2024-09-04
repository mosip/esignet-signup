/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.helper;

import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.kernel.core.util.UUIDUtils;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.services.CacheUtilService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static io.mosip.kernel.core.util.UUIDUtils.NAMESPACE_OID;

@Slf4j
@Component
public class CryptoHelper {

    public static final String ALIAS_CACHE_KEY = "CURRENT_ACTIVE_ALIAS";

    @Value("${mosip.signup.cache.symmetric-algorithm-name}")
    private String symmetricAlgorithm;

    @Value("${mosip.signup.cache.symmetric-key.algorithm-name:AES}")
    private String symmetricKeyAlgorithm;

    @Value("${mosip.signup.cache.symmetric-key.size:256}")
    private int symmetricKeySize;

    @Autowired
    private CacheUtilService cacheUtilService;

    public String symmetricEncrypt(String data) {
        try {

            String keyAlias = getActiveKeyAlias();
            SecretKey secretKey = getSecretKey(keyAlias);

            Cipher cipher = Cipher.getInstance(symmetricAlgorithm);
            byte[] initializationVector = IdentityProviderUtil.generateSalt(cipher.getBlockSize());
            byte[] secretDataBytes = data.getBytes(StandardCharsets.UTF_8);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(initializationVector));
            byte[] encryptedBytes = cipher.doFinal(secretDataBytes, 0, secretDataBytes.length);

            byte[] keyAliasBytes = keyAlias.getBytes();

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
            Cipher cipher = Cipher.getInstance(symmetricAlgorithm);

            byte[] data = IdentityProviderUtil.b64Decode(encryptedData);
            byte[] keyAlias = Arrays.copyOfRange(data, data.length-36, data.length);
            byte[] iv = Arrays.copyOfRange(data, data.length-(cipher.getBlockSize()+36), data.length-36);
            byte[] encryptedBytes = Arrays.copyOfRange(data, 0, data.length-(cipher.getBlockSize()+36));

            String encodedSecretKey = cacheUtilService.getSecretKey(new String(keyAlias));
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(IdentityProviderUtil.b64Decode(encodedSecretKey), "AES"),
                    new IvParameterSpec(iv));
            return new String(cipher.doFinal(encryptedBytes, 0, encryptedBytes.length));
        } catch (Exception e) {
            log.error("Error Cipher Operations for provided secret data.", e);
            throw new SignUpException("crypto_error");
        }
    }


    public SecretKey getSecretKey(String alias) {
        String encodedSecretKey = cacheUtilService.getSecretKey(alias);
        return new SecretKeySpec(IdentityProviderUtil.b64Decode(encodedSecretKey), "AES");
    }

    private String getActiveKeyAlias() {
        String alias = cacheUtilService.getActiveKeyAlias();
        if(alias != null)
            return alias;

        log.debug("No active alias found, generating new alias and AES key.");
        alias = UUIDUtils.getUUID(NAMESPACE_OID, "signup-service").toString();
        generateSecretKey(alias);
        return alias;
    }

    private void generateSecretKey(String alias) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(symmetricKeyAlgorithm);
            keyGenerator.init(symmetricKeySize);
            cacheUtilService.setSecretKey(alias, IdentityProviderUtil.b64Encode(keyGenerator.generateKey().getEncoded()));
            cacheUtilService.setActiveKeyAlias(ALIAS_CACHE_KEY, alias);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating secret key", e);
            throw new SignUpException("crypto_error");
        }
    }
}
