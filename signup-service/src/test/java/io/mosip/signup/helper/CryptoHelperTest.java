package io.mosip.signup.helper;

import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.services.CacheUtilService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Cipher;

@RunWith(MockitoJUnitRunner.class)
public class CryptoHelperTest {

    @InjectMocks
    private CryptoHelper cryptoHelper;

    @Mock
    private CacheUtilService cacheUtilService;

    @Mock
    private Cipher cipher;

    @Test
    public void symmetricEncryptWithValidDetails_thenPass() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(cryptoHelper, "symmetricAlgorithm", "AES/CFB/PKCS5Padding");
        String testData = "testData";
        String keyAlias = "alias";
        Mockito.when(cacheUtilService.getActiveKeyAlias()).thenReturn(keyAlias);
        Mockito.when(cacheUtilService.getSecretKey(Mockito.anyString())).thenReturn("base64EncodedSecretKeyForTesting");

        String result = cryptoHelper.symmetricEncrypt(testData);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertTrue(IdentityProviderUtil.b64Decode(result).length > 0);
    }

    @Test
    public void symmetricEncrypt_withInvaildDetails_thenFail() {

        ReflectionTestUtils.setField(cryptoHelper, "symmetricKeyAlgorithm", "AES");
        ReflectionTestUtils.setField(cryptoHelper, "symmetricKeySize", 128);
        Mockito.when(cacheUtilService.getActiveKeyAlias()).thenReturn(null);
        Assert.assertThrows(SignUpException.class, () -> cryptoHelper.symmetricEncrypt("anyData"));
    }

    @Test
    public void symmetricEncrypt_WithInvalidAlgorithm_thenFail() {

        ReflectionTestUtils.setField(cryptoHelper, "symmetricKeyAlgorithm", "AESST");
        ReflectionTestUtils.setField(cryptoHelper, "symmetricKeySize", 128);
        Mockito.when(cacheUtilService.getActiveKeyAlias()).thenReturn(null);
        Assert.assertThrows(SignUpException.class, () -> cryptoHelper.symmetricEncrypt("anyData"));
    }

    @Test
    public void symmetricDecrypt_withInValidDetails_thenPass(){

        ReflectionTestUtils.setField(cryptoHelper, "symmetricAlgorithm", "AES/CFB/PKCS5Padding");
        String testData = "base64EncodedSecretKeyDatabase64EncodedSecretKeyForTesting";
        Mockito.when(cacheUtilService.getSecretKey(Mockito.anyString())).thenReturn("base64EncodedSecretKeyForTesting");
        String encryptedData = IdentityProviderUtil.b64Encode(testData.getBytes());
        Assert.assertThrows(Exception.class, () -> cryptoHelper.symmetricDecrypt(encryptedData));

    }
}
