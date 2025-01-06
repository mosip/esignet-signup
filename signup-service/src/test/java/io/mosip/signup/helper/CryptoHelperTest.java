package io.mosip.signup.helper;

import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.services.CacheUtilService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CryptoHelperTest {

    @InjectMocks
    private CryptoHelper cryptoHelper;

    @Mock
    private CacheUtilService cacheUtilService;

    private static String symmetricAlgorithm = "AES/CFB/PKCS5Padding";
    private static String symmetricKeyAlgorithm = "AES";
    private static int symmetricKeySize = 256;

    String keyAlias = "aced6829-63bb-5b28-8898-64efd90a70fa";
    private static String secretKey;

    static {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance(symmetricKeyAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyGenerator.init(symmetricKeySize);
        secretKey = IdentityProviderUtil.b64Encode(keyGenerator.generateKey().getEncoded());
    }

    @Before
    public void setUp()  {
        when(cacheUtilService.getSecretKey(Mockito.anyString())).thenReturn(secretKey).thenReturn(secretKey);

        ReflectionTestUtils.setField(cryptoHelper, "symmetricAlgorithm", symmetricAlgorithm);
        ReflectionTestUtils.setField(cryptoHelper, "symmetricKeyAlgorithm", symmetricKeyAlgorithm);
        ReflectionTestUtils.setField(cryptoHelper, "symmetricKeySize", symmetricKeySize);
    }

    @Test
    public void symmetricEncrypt_withValidInput_thenPass() {
        String data = "test data test fatata";
        String encryptedData = cryptoHelper.symmetricEncrypt(data);

        assertNotNull(encryptedData);
        verify(cacheUtilService, times(1)).getActiveKeyAlias();
        verify(cacheUtilService, times(1)).getSecretKey(keyAlias);

        String decryptedData = cryptoHelper.symmetricDecrypt(encryptedData);
        assertNotNull(decryptedData);
        assertEquals(data, decryptedData);
        verify(cacheUtilService, times(1)).getActiveKeyAlias();
        verify(cacheUtilService, times(2)).getSecretKey(keyAlias);
    }
}

