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
    private String defaultLanguage = "en";
    private List<String> encodedLangCodes = List.of("es");

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(notificationHelper, "sendNotificationEndpoint", sendNotificationEndpoint);
        ReflectionTestUtils.setField(notificationHelper, "defaultLanguage", defaultLanguage);
        ReflectionTestUtils.setField(notificationHelper, "encodedLangCodes", encodedLangCodes);
        ReflectionTestUtils.setField(notificationHelper, "identifierPrefix", "");
    }

    @Test
    public void testSendSMSNotification_withValidInput_thenPass() {
        String locale = "eng";
        String templateKey = "mosip.signup.sms-notification-template.send-otp";
        String message = "Hello, {{name}}!";

        when(environment.getProperty(templateKey + "." + locale)).thenReturn(Base64.getEncoder().encodeToString(message.getBytes()));
        Map<String, String> params = new HashMap<>();
        params.put("{{name}}", "John");

        RestResponseWrapper<NotificationResponse> responseWrapper = new RestResponseWrapper<>();
        ResponseEntity<RestResponseWrapper<NotificationResponse>> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getBody()).thenReturn(responseWrapper);
        when(selfTokenRestTemplate.exchange(
                eq(sendNotificationEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        notificationHelper.sendSMSNotification("1234567890", locale, templateKey, params);

        verify(selfTokenRestTemplate, times(1)).exchange(
                eq(sendNotificationEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class));
    }

    @Test(expected = SignUpException.class)
    public void testSendSMSNotification_onRestException_thenFail() {
        String locale = "eng";
        String templateKey = "mosip.signup.sms-notification-template.send-otp";
        String message = "Hello, {{name}}!";

        when(environment.getProperty(templateKey + "." + locale)).thenReturn(message);

        when(selfTokenRestTemplate.exchange(
                eq(sendNotificationEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("Error in RestTemplate"));

        notificationHelper.sendSMSNotification("1234567890", locale, templateKey, null);
    }

    @Test
    public void testSendSMSNotification_withNullLocale_thenPass() { //fallback to default language
        String locale = null;
        String templateKey = "mosip.signup.sms-notification-template.send-otp";
        String message = "Hello, {{name}}!";

        when(environment.getProperty(templateKey + "." + defaultLanguage)).thenReturn(Base64.getEncoder().encodeToString(message.getBytes()));

        Map<String, String> params = new HashMap<>();
        params.put("{{name}}", "John");

        RestResponseWrapper<NotificationResponse> responseWrapper = new RestResponseWrapper<>();
        ResponseEntity<RestResponseWrapper<NotificationResponse>> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getBody()).thenReturn(responseWrapper);
        when(selfTokenRestTemplate.exchange(
                eq(sendNotificationEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        notificationHelper.sendSMSNotification("1234567890", locale, templateKey, params);

        verify(selfTokenRestTemplate, times(1)).exchange(
                eq(sendNotificationEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class));
    }

    @Test
    public void testSendSMSNotificationAsync() {
        // Verify that the async method simply delegates to the sync method
        NotificationHelper spyNotificationHelper = spy(notificationHelper);

        RestResponseWrapper<NotificationResponse> responseWrapper = new RestResponseWrapper<>();
        ResponseEntity<RestResponseWrapper<NotificationResponse>> responseEntity = mock(ResponseEntity.class);
        when(responseEntity.getBody()).thenReturn(responseWrapper);
        when(selfTokenRestTemplate.exchange(
                eq(sendNotificationEndpoint),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        spyNotificationHelper.sendSMSNotificationAsync("1234567890", "en", "sms.templateKey", null);
        verify(spyNotificationHelper, times(1)).sendSMSNotification("1234567890", "en", "sms.templateKey", null);
    }
}

