/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.helper;

import io.mosip.signup.dto.AuditRequest;
import io.mosip.signup.dto.AuditResponse;
import io.mosip.signup.dto.RestRequestWrapper;
import io.mosip.signup.dto.RestResponseWrapper;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.util.AuditEvent;
import io.mosip.signup.util.AuditEventType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static io.mosip.esignet.core.util.IdentityProviderUtil.getUTCDateTime;

@Slf4j
@Component
public class AuditHelper {

    @Autowired
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate selfTokenRestTemplate;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${mosip.signup.audit-endpoint}")
    String sendAuditTransactionsUrl;

    @Value("${mosip.signup.audit.description.max-length}")
    Integer auditDescriptionMaxLength;

    public void sendAuditTransaction(AuditEvent auditEvent, AuditEventType eventType, String transactionId,
                                     SignUpException signUpException){

        RestRequestWrapper<AuditRequest> restRequestWrapper = new RestRequestWrapper<>();
        restRequestWrapper.setRequesttime(getUTCDateTime());

        String description = signUpException != null ?
                ExceptionUtils.getStackTrace(signUpException) : auditEvent.toString() + " " + eventType.toString();
        if (description != null && description.length() > auditDescriptionMaxLength) {
            description = description.substring(0, auditDescriptionMaxLength);
        }
        AuditRequest auditRequest = new AuditRequest(auditEvent, eventType, applicationName, "no-user",
                transactionId, "EsignetSignUpService", description);
        restRequestWrapper.setRequest(auditRequest);

        try{
            selfTokenRestTemplate.exchange(sendAuditTransactionsUrl, HttpMethod.POST, new HttpEntity<>(restRequestWrapper),
                    new ParameterizedTypeReference<RestResponseWrapper<AuditResponse>>(){}).getBody();
        }catch (RestClientException restClientException){
            log.error("An error occurred in sendAuditTransaction", restClientException);
        }
    }
}
