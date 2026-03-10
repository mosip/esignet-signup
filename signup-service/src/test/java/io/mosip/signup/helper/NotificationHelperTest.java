package io.mosip.signup.helper;

import io.mosip.signup.dto.NotificationResponse;
import io.mosip.signup.dto.RestResponseWrapper;
import io.mosip.signup.exception.SignUpException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NotificationHelperTest {

    @InjectMocks
    private NotificationHelper notificationHelper;

    @Mock
    private RestTemplate selfTokenRestTemplate;

    @Mock
    private Environment environment;

    private String sendNotificationEndpoint = "http://test.endpoint.com/send-notification";
    private String defaultLanguage = "eng";
    private List<String> encodedLangCodes = List.of("khm");

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(notificationHelper, "sendNotificationEndpoint", sendNotificationEndpoint);
        ReflectionTestUtils.setField(notificationHelper, "defaultLanguage", defaultLanguage);
        ReflectionTestUtils.setField(notificationHelper, "encodedLangCodes", encodedLangCodes);
        ReflectionTestUtils.setField(notificationHelper, "identifierPrefix", "");
        ReflectionTestUtils.setField(notificationHelper, "defaultChannel", "sms");
        ReflectionTestUtils.setField(notificationHelper, "removeCountryCode", false);
    }

    @Test
    public void sendNotification_withValidInput_thenPass() {
        String locale = "eng";
        String templateKey = "send-otp";
        String message = "Hello, {{name}}!";

        when(environment.getProperty("mosip.signup.sms-notification-template." + templateKey + "." + locale)).thenReturn(message);
        Map<String, String> params = new HashMap<>();
        params.put("{{name}}", "John");

        RestResponseWrapper<NotificationResponse> responseWrapper = new RestResponseWrapper<>();
        ResponseEntity<RestResponseWrapper<NotificationResponse>> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getBody()).thenReturn(responseWrapper);
        when(selfTokenRestTemplate.exchange(eq(sendNotificationEndpoint), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        notificationHelper.sendNotification("1234567890", locale, templateKey, params);

        verify(selfTokenRestTemplate, times(1)).exchange(eq(sendNotificationEndpoint), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test(expected = SignUpException.class)
    public void sendNotification_onRestException_thenFail() {
        String locale = "eng";
        String templateKey = "send-otp";
        String message = "Hello, {{name}}!";

        when(environment.getProperty("mosip.signup.sms-notification-template." + templateKey + "." + locale)).thenReturn(message);

        when(selfTokenRestTemplate.exchange(eq(sendNotificationEndpoint), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenThrow(new RestClientException("Error in RestTemplate"));

        notificationHelper.sendNotification("1234567890", locale, templateKey, null);
    }

    @Test
    public void sendNotification_withNullLocale_thenPass() { //fallback to default language
        String locale = null;
        String templateKey = "send-otp";
        String message = "Hello, {{name}}!";

        when(environment.getProperty("mosip.signup.sms-notification-template." + templateKey + "." + defaultLanguage)).thenReturn(message);

        Map<String, String> params = new HashMap<>();
        params.put("{{name}}", "John");

        RestResponseWrapper<NotificationResponse> responseWrapper = new RestResponseWrapper<>();
        ResponseEntity<RestResponseWrapper<NotificationResponse>> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getBody()).thenReturn(responseWrapper);
        when(selfTokenRestTemplate.exchange(eq(sendNotificationEndpoint), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        notificationHelper.sendNotification("1234567890", locale, templateKey, params);

        verify(selfTokenRestTemplate, times(1)).exchange(eq(sendNotificationEndpoint), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void sendNotificationAsync() {
        NotificationHelper spyNotificationHelper = spy(notificationHelper);

        String templateKey = "send-otp";
        String locale = "eng";
        String message = "Hello";

        when(environment.getProperty("mosip.signup.sms-notification-template." + templateKey + "." + locale)).thenReturn(message);

        RestResponseWrapper<NotificationResponse> responseWrapper = new RestResponseWrapper<>();
        ResponseEntity<RestResponseWrapper<NotificationResponse>> responseEntity = mock(ResponseEntity.class);

        when(responseEntity.getBody()).thenReturn(responseWrapper);

        when(selfTokenRestTemplate.exchange(eq(sendNotificationEndpoint), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        spyNotificationHelper.sendNotificationAsync("1234567890", locale, templateKey, null);

        verify(spyNotificationHelper, times(1)).sendNotification("1234567890", locale, templateKey, null);
    }

    @Test
    public void sendNotification_withEmailChannel_thenPass() {
        ReflectionTestUtils.setField(notificationHelper, "defaultChannel", "email");

        String locale = "eng";
        String templateKey = "send-otp";

        when(environment.getProperty("mosip.signup.email-notification-template.subject." + templateKey + "." + locale)).thenReturn("OTP Verification");
        when(environment.getProperty("mosip.signup.email-notification-template.content." + templateKey + "." + locale)).thenReturn("Use {challenge} to verify your account.");

        RestResponseWrapper<NotificationResponse> responseWrapper = new RestResponseWrapper<>();
        ResponseEntity<RestResponseWrapper<NotificationResponse>> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getBody()).thenReturn(responseWrapper);
        when(selfTokenRestTemplate.exchange(eq(sendNotificationEndpoint), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        notificationHelper.sendNotification("user@example.com", locale, templateKey, null);

        verify(selfTokenRestTemplate, times(1)).exchange(eq(sendNotificationEndpoint), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test(expected = SignUpException.class)
    public void sendNotification_withUnsupportedChannel_thenFail() {
        ReflectionTestUtils.setField(notificationHelper, "defaultChannel", "push");

        notificationHelper.sendNotification("user@example.com", "eng", "send-otp", null);
    }

    @Test(expected = SignUpException.class)
    public void sendNotification_templateNotFound_thenFail() {
        String locale = "eng";
        String templateKey = "send-otp";

        when(environment.getProperty("mosip.signup.sms-notification-template." + templateKey + "." + locale)).thenReturn(null);  // property not configured

        notificationHelper.sendNotification("+85512345678", locale, templateKey, null);
    }

    @Test
    public void sendNotification_withRemoveCountryCode_thenPass() {
        ReflectionTestUtils.setField(notificationHelper, "removeCountryCode", true);
        ReflectionTestUtils.setField(notificationHelper, "identifierPrefix", "+855");

        String locale = "eng";
        String templateKey = "send-otp";
        String message = "Use {challenge} to verify your account.";

        when(environment.getProperty("mosip.signup.sms-notification-template." + templateKey + "." + locale)).thenReturn(message);

        RestResponseWrapper<NotificationResponse> responseWrapper = new RestResponseWrapper<>();
        ResponseEntity<RestResponseWrapper<NotificationResponse>> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getBody()).thenReturn(responseWrapper);
        when(selfTokenRestTemplate.exchange(eq(sendNotificationEndpoint), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        // +855 prefix should be stripped → 12345678
        notificationHelper.sendNotification("+85512345678", locale, templateKey, null);

        verify(selfTokenRestTemplate, times(1)).exchange(eq(sendNotificationEndpoint), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void sendNotification_withEncodedKhmTemplate_thenPass() {
        String locale = "khm";
        String templateKey = "send-otp";
        String message = "Use {challenge} to verify your account.";

        when(environment.getProperty("mosip.signup.sms-notification-template." + templateKey + "." + locale)).thenReturn(Base64.getEncoder().encodeToString(message.getBytes()));

        RestResponseWrapper<NotificationResponse> responseWrapper = new RestResponseWrapper<>();
        ResponseEntity<RestResponseWrapper<NotificationResponse>> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getBody()).thenReturn(responseWrapper);
        when(selfTokenRestTemplate.exchange(eq(sendNotificationEndpoint), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        notificationHelper.sendNotification("+85512345678", locale, templateKey, null);

        verify(selfTokenRestTemplate, times(1)).exchange(eq(sendNotificationEndpoint), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }
}