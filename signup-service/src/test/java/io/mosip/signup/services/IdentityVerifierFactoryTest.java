package io.mosip.signup.services;

import io.mosip.signup.api.exception.IdentityVerifierException;
import io.mosip.signup.api.spi.IdentityVerifierPlugin;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.stream.Stream;

import static io.mosip.signup.api.util.ErrorConstants.PLUGIN_NOT_FOUND;

@RunWith(MockitoJUnitRunner.class)
public class IdentityVerifierFactoryTest{

    @InjectMocks
    private IdentityVerifierFactory identityVerifierFactory;

    @Mock
    private List<IdentityVerifierPlugin> identityVerifiers;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getIdentityVerifierWhenPluginExists_thenPass() {
        String id = "123";
        IdentityVerifierPlugin plugin = Mockito.mock(IdentityVerifierPlugin.class);
        Mockito.when(plugin.getVerifierId()).thenReturn(id);
        Mockito.when(identityVerifiers.stream()
                .filter(idv -> idv.getVerifierId().equals(id)))
                .thenReturn(Stream.of(plugin));
        IdentityVerifierPlugin result = identityVerifierFactory.getIdentityVerifier(id);
        Assert.assertNotNull(result);
        Assert.assertEquals(plugin, result);
    }

    @Test
    public void getIdentityVerifierWhenPluginDoesNotExist_thenFail() {
        String id = "123";
        Mockito.when(identityVerifiers.stream()
                .filter(idv -> idv.getVerifierId().equals(id)))
                .thenReturn(Stream.empty());
        try {
            identityVerifierFactory.getIdentityVerifier(id);
            Assert.fail();
        }catch (IdentityVerifierException e){
            Assert.assertEquals(PLUGIN_NOT_FOUND,e.getMessage());
        }
    }

}
