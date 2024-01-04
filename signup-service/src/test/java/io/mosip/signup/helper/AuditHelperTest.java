package io.mosip.signup.helper;

import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.util.AuditEventType;
import io.mosip.signup.util.AuditEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditHelperTest {

    @InjectMocks
    private AuditHelper auditHelper;

    @Mock
    private RestTemplate selfTokenRestTemplate;

    private final String sendAuditTransactionsUrl = "sendAuditTransactionsUrl";

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(auditHelper, sendAuditTransactionsUrl, sendAuditTransactionsUrl);
        ReflectionTestUtils.setField(auditHelper, "auditDescriptionMaxLength", 2048);
    }

    @Test
    void sendAuditTransaction_thenPass() {

        when(selfTokenRestTemplate.exchange(
                eq(sendAuditTransactionsUrl), eq(HttpMethod.POST),
                any(), any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        auditHelper.sendAuditTransaction(AuditEvent.REGISTER, AuditEventType.ERROR,
                "transactionId", new SignUpException(ErrorConstants.UNKNOWN_ERROR));
        verify(selfTokenRestTemplate, times(1)).exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class));
    }

    @Test
    void sendAuditTransaction_shouldHandleRestClientException() {

        when(selfTokenRestTemplate.exchange(eq(sendAuditTransactionsUrl), any(), any(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("Simulated RestClientException"));

        assertDoesNotThrow(() -> auditHelper.sendAuditTransaction(AuditEvent.REGISTER, AuditEventType.ERROR, "transactionId", null));

        verify(selfTokenRestTemplate, times(1)).exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class));
    }
}
