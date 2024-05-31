package io.mosip.signup.services;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.nimbusds.oauth2.sdk.ParseException;
import io.mosip.signup.dto.IdentityVerifierDetail;
import io.mosip.signup.dto.InitiateIdentityVerificationRequest;
import io.mosip.signup.dto.InitiateIdentityVerificationResponse;
import io.mosip.signup.util.ErrorConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import static com.github.tomakehurst.wiremock.client.WireMock.*;


@RunWith(MockitoJUnitRunner.class)
public class IdentityVerificationServiceTest {

    @InjectMocks
    IdentityVerificationService identityVerificationService;

    @Mock
    CacheUtilService cacheUtilService;

    private WireMockServer wireMockServer;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        String oauthIssuerUri = "http://localhost:" + wireMockServer.port() + "/signup.dev.mosip.net";
        ReflectionTestUtils.setField(identityVerificationService, "privateKeyAlias", "privateKeyAlias");
        ReflectionTestUtils.setField(identityVerificationService, "p12FilePath", "keystore.p12");
        ReflectionTestUtils.setField(identityVerificationService, "audience", "/v1/esignet/oauth/token");
        ReflectionTestUtils.setField(identityVerificationService, "p12FilePassword", "password");
        ReflectionTestUtils.setField(identityVerificationService, "oauthClientId", "clientId");
        ReflectionTestUtils.setField(identityVerificationService, "oauthRedirectUri", "https://signup.dev.mosip.net/identity-verification");
        ReflectionTestUtils.setField(identityVerificationService, "oauthIssuerUri", oauthIssuerUri);
    }


    @Test
    public void initiateIdentityVerification_withValidDetails_thenPass() throws ParseException, IOException {

        InitiateIdentityVerificationRequest request = new InitiateIdentityVerificationRequest();
        request.setAuthorizationCode("authCode");
        request.setState("state");

        wireMockServer.stubFor(post(urlEqualTo("/signup.dev.mosip.net/v1/esignet/oauth/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "    \"id_token\": \"eyJraWQiOiJkdmt6enRuanJ3WS11azU1V3hGN0FCNWFTOHFPODFGd2o5T05UTERZLVI0IiwiYWxnIjoiUlMyNTYifQ.eyJhdF9oYXNoIjoid2VCMlhGUHE0eFpHNF9VUFhYckNpZyIsInN1YiI6Ijk3Q3NiYk01amY3MHBMbmpWaW9PaGNvclFiMXVWbE5leUNXM2ZRT2VuY00iLCJhdWQiOiIxMjM0NTY3ODkwIiwiYWNyIjoibW9zaXA6aWRwOmFjcjpnZW5lcmF0ZWQtY29kZSIsImF1dGhfdGltZSI6MTcxNzA2MjU5OCwiaXNzIjoiaHR0cDpcL1wvbG9jYWxob3N0OjgwODhcL3YxXC9lc2lnbmV0IiwiZXhwIjoxNzE3MDY2MjAxLCJpYXQiOjE3MTcwNjI2MDEsIm5vbmNlIjoiOTczZWllbGp6bmcifQ.Yopt54_l9CBZUcYA3cWxXugnMfwgOkn0H8QEFtaQBTq_SkBoqabt0_5cGMoc7U_WhUnhRFfWNZKuUrrmCZyrnOnrmh_-qQ004lql_GfXc-vrWSCVYx9FK_G5E115a6ltT_XtH3Ur_9Fw7QOk08WIzBU6BOi-lz7Q46lGzAR2tGVQ6tXvYjpRtM1WNkyL33KJNGS2bBGqglK7CzPm3Pj7tyM9nXuusNOGVed_PgbG-Ur7ItpZWy1feDd8WmynXibM6kc95cCg2a4GHpJxjb437ls_OYEHjDm6F0eWyAdemp6mXLIsj7fZYu_razwaXYSW2ZKg6XQLyqZfHa2j5AWnXA\",\n" +
                                "    \"token_type\": \"Bearer\",\n" +
                                "    \"access_token\": \"eyJraWQiOiJkdmt6enRuanJ3WS11azU1V3hGN0FCNWFTOHFPODFGd2o5T05UTERZLVI0IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiI5N0NzYmJNNWpmNzBwTG5qVmlvT2hjb3JRYjF1VmxOZXlDVzNmUU9lbmNNIiwiYXVkIjoiMTIzNDU2Nzg5MCIsImlzcyI6Imh0dHA6XC9cL2xvY2FsaG9zdDo4MDg4XC92MVwvZXNpZ25ldCIsImV4cCI6MTcxNzA2NjIwMSwiaWF0IjoxNzE3MDYyNjAxLCJjbGllbnRfaWQiOiIxMjM0NTY3ODkwIn0.dVST9YDnTksOs764zJlYq4Qxam2foY6nbNhHadUMBWEubzXOV85g9oWOOoRtyJTHPpzIWGLRQ_XrO4GyKN0RvG2me9atwTIMAUrKAh2sPusNiZSLkaAtrOu1Oppfe0ob03ozJVmHtwaL8fPwEQ1icJktjEqSDpTMgZG333K9rZaA8wSqVExaF94PvONWSxrv9EmfIMJssJdIRrqWPqPbrh6Jx-WYMe3pdGyxZTh_V6EeCvbMbHTpfG0Kg0ZD8BcsAaDmw1Sk6fehEiW2RuJK_0Vp_4I9_IsOzuSYcFafxhNpkJjnCLNfnqvXVm5qXi-x2WB9l54RHnpTrmQ3yitWZg\",\n" +
                                "    \"expires_in\": 3600\n" +
                                "}")));

        IdentityVerifierDetail [] identityVerifierDetails = new IdentityVerifierDetail[1];
        Mockito.when(cacheUtilService.getIdentityVerifierDetails()).thenReturn(identityVerifierDetails);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        InitiateIdentityVerificationResponse result = identityVerificationService.initiateIdentityVerification(request, response);

        Assert.assertNotNull(result);
    }

    @Test
    public void initiateIdentityVerification_withInvalidPrivateKeyPassword_thenFail() throws ParseException, IOException {

        ReflectionTestUtils.setField(identityVerificationService, "p12FilePassword", "wrongpassword");
        InitiateIdentityVerificationRequest request = new InitiateIdentityVerificationRequest();
        request.setAuthorizationCode("authCode");
        request.setState("state");
        try {
            identityVerificationService.initiateIdentityVerification(request,null);
        }catch (Exception e) {
            Assert.assertEquals(ErrorConstants.TOKEN_EXCHANGE_FAILED, e.getMessage());
        }
    }
}
