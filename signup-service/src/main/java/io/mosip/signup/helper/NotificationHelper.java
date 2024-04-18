package io.mosip.signup.helper;

import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.NotificationRequest;
import io.mosip.signup.dto.NotificationResponse;
import io.mosip.signup.dto.RestRequestWrapper;
import io.mosip.signup.dto.RestResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class NotificationHelper {

    @Autowired
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate selfTokenRestTemplate;

    @Autowired
    private Environment environment;

    @Value("${mosip.signup.send-notification.endpoint}")
    private String sendNotificationEndpoint;

    @Value("{${mosip.signup.default-language}")
    private String defaultLanguage;

    @Value("#{${mosip.signup.sms-notification-template.encoded-langcodes}}")
    private List<String> encodedLangCodes;

    public RestResponseWrapper<NotificationResponse> sendSMSNotification
            (String number, String locale, String templateKey, Map<String, String> params){

        locale = locale != null ? locale : defaultLanguage;

        String message = encodedLangCodes.contains(locale)?
                new String(Base64.getDecoder().decode(environment.getProperty(templateKey + "." + locale))):
                environment.getProperty(templateKey + "." + locale);

        if (params != null && message != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                message = message.replace(entry.getKey(), entry.getValue());
            }
        }

        NotificationRequest notificationRequest = new NotificationRequest(number.substring(4), message);

        RestRequestWrapper<NotificationRequest> restRequestWrapper = new RestRequestWrapper<>();
        restRequestWrapper.setRequesttime(IdentityProviderUtil.getUTCDateTime());
        restRequestWrapper.setRequest(notificationRequest);

        return selfTokenRestTemplate.exchange(sendNotificationEndpoint,
                        HttpMethod.POST,
                        new HttpEntity<>(restRequestWrapper),
                        new ParameterizedTypeReference<RestResponseWrapper<NotificationResponse>>(){}).getBody();
    }

    @Async
    public CompletableFuture<RestResponseWrapper<NotificationResponse>> sendSMSNotificationAsync
            (String number, String locale, String templateKey, Map<String, String> params){
        return CompletableFuture.supplyAsync(() -> sendSMSNotification(number, locale, templateKey, params));
    }
}
