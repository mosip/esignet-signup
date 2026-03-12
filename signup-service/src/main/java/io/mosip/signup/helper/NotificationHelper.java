/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.helper;

import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.NotificationRequest;
import io.mosip.signup.dto.NotificationResponse;
import io.mosip.signup.dto.RestRequestWrapper;
import io.mosip.signup.dto.RestResponseWrapper;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.util.ErrorConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import static io.mosip.signup.util.SignUpConstants.*;

@Slf4j
@Component
public class NotificationHelper {

    @Autowired
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate selfTokenRestTemplate;

    @Autowired
    private Environment environment;

    @Value("${mosip.signup.send-notification.endpoint}")
    private String sendNotificationEndpoint;

    @Value("${mosip.signup.default-language}")
    private String defaultLanguage;

    @Value("#{${mosip.signup.notification-template.encoded-langcodes}}")
    private List<String> encodedLangCodes;

    @Value("${mosip.signup.identifier.prefix:}")
    private String identifierPrefix;

    @Value("${mosip.signup.sms-notification.remove-country-code:false}")
    private boolean removeCountryCode;

    @Value("${mosip.signup.send-notification.channel:sms}")
    private String defaultChannel;

    public void sendNotification(String identifier, String locale, String templateKey, Map<String, String> params) {
        locale = locale != null ? locale : defaultLanguage;
        if (EMAIL_CHANNEL.equalsIgnoreCase(defaultChannel)) {
            sendEmailNotification(identifier, locale, templateKey, params);
        } else if (SMS_CHANNEL.equalsIgnoreCase(defaultChannel)) {
            sendSMSNotification(identifier, locale, templateKey, params);
        } else {
            log.error("Unsupported notification channel configured: {}", defaultChannel);
            throw new SignUpException(ErrorConstants.INVALID_NOTIFICATION_CHANNEL);
        }
    }

    @Async
    public void sendNotificationAsync(String identifier, String locale, String templateKey, Map<String, String> params) {
        sendNotification(identifier, locale, templateKey, params);
    }

    private void sendEmailNotification(String identifier, String locale, String templateKey, Map<String, String> params) {
        boolean isEncoded = encodedLangCodes.contains(locale);
        String subject = resolveTemplate(EMAIL_SUBJECT_PROPERTY_PREFIX + templateKey + "." + locale, isEncoded, params);
        String content = resolveTemplate(EMAIL_CONTENT_PROPERTY_PREFIX + templateKey + "." + locale, isEncoded, params);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("mailTo", identifier);
        formData.add("mailSubject", subject);
        formData.add("mailContent", content);
        try {
            RestResponseWrapper<NotificationResponse> responseWrapper = selfTokenRestTemplate.exchange(sendNotificationEndpoint, HttpMethod.POST, new HttpEntity<>(formData, headers), new ParameterizedTypeReference<RestResponseWrapper<NotificationResponse>>() {
            }).getBody();
            validateResponse(responseWrapper, "Email");
        } catch (RestClientException e) {
            log.error("Failed to send email notification", e);
            throw new SignUpException(ErrorConstants.OTP_NOTIFICATION_FAILED);
        }
    }

    private void sendSMSNotification(String identifier, String locale, String templateKey, Map<String, String> params) {
        boolean isEncoded = encodedLangCodes.contains(locale);
        String message = resolveTemplate(SMS_TEMPLATE_PROPERTY_PREFIX + templateKey + "." + locale, isEncoded, params);
        String phoneNumber = identifier;
        if (removeCountryCode && identifier.startsWith(identifierPrefix)) {
            phoneNumber = identifier.substring(identifierPrefix.length());
        }
        NotificationRequest notificationRequest = new NotificationRequest(phoneNumber, message);
        RestRequestWrapper<NotificationRequest> restRequestWrapper = new RestRequestWrapper<>();
        restRequestWrapper.setRequesttime(IdentityProviderUtil.getUTCDateTime());
        restRequestWrapper.setRequest(notificationRequest);
        try {
            RestResponseWrapper<NotificationResponse> responseWrapper = selfTokenRestTemplate.exchange(sendNotificationEndpoint, HttpMethod.POST, new HttpEntity<>(restRequestWrapper), new ParameterizedTypeReference<RestResponseWrapper<NotificationResponse>>() {
            }).getBody();
            validateResponse(responseWrapper, "SMS");
        } catch (RestClientException e) {
            log.error("Failed to send SMS notification", e);
            throw new SignUpException(ErrorConstants.OTP_NOTIFICATION_FAILED);
        }
    }

    private void validateResponse(RestResponseWrapper<NotificationResponse> responseWrapper, String channel) {
        if (responseWrapper == null || (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty())) {
            log.error("{} notification failed. Response: {}", channel, responseWrapper);
            throw new SignUpException(ErrorConstants.OTP_NOTIFICATION_FAILED);
        }
        log.debug("{} notification response -> {}", channel, responseWrapper);
    }

    private String resolveTemplate(String templateKey, boolean isEncoded, Map<String, String> params) {
        String rawTemplate = environment.getProperty(templateKey);
        if (rawTemplate == null) {
            log.error("Notification template not configured: {}", templateKey);
            throw new SignUpException(ErrorConstants.NOTIFICATION_TEMPLATE_NOT_FOUND);
        }

        String template;
        try {
            template = isEncoded ? new String(Base64.getDecoder().decode(rawTemplate)) : rawTemplate;
        } catch (IllegalArgumentException e) {
            log.error("Invalid encoded notification template: {}", templateKey, e);
            throw new SignUpException(ErrorConstants.NOTIFICATION_TEMPLATE_NOT_FOUND);
        }
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                template = template.replace(entry.getKey(), entry.getValue());
            }
        }
        return template;
    }
}
