package io.mosip.signup.controllers;
import static io.mosip.esignet.core.constants.Constants.UTC_DATETIME_PATTERN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mosip.signup.dto.*;
import io.mosip.esignet.core.dto.RequestWrapper;
import io.mosip.signup.dto.InitiateIdentityVerificationRequest;
import io.mosip.signup.dto.InitiateIdentityVerificationResponse;
import io.mosip.signup.dto.SlotRequest;
import io.mosip.signup.dto.SlotResponse;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.helper.AuditHelper;
import io.mosip.signup.services.IdentityVerificationService;
import io.mosip.signup.util.SignUpConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RunWith(SpringRunner.class)
@WebMvcTest(value = IdentityVerificationController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ActiveProfiles(value = {"test"})
public class IdentityVerificationControllerTest {

    @MockBean
    IdentityVerificationService identityVerificationService;

    @MockBean
    RestTemplate restTemplate;

    @MockBean
    AuditHelper auditHelper;

    @Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void initiateIdentityVerification_withValidDetails_thenPass() throws Exception {
        IdentityVerifierDetail detail = new IdentityVerifierDetail();
        detail.setId("id");
        InitiateIdentityVerificationResponse response = new InitiateIdentityVerificationResponse();
        response.setIdentityVerifiers(new IdentityVerifierDetail[]{detail});
        InitiateIdentityVerificationRequest initiateIdentityVerificationRequest=new InitiateIdentityVerificationRequest();
        initiateIdentityVerificationRequest.setState("state");
        initiateIdentityVerificationRequest.setAuthorizationCode("authcode");
        RequestWrapper<InitiateIdentityVerificationRequest> requestWrapper = new RequestWrapper<>();
        ZonedDateTime requestTime = ZonedDateTime.now(ZoneOffset.UTC);
        requestWrapper.setRequest(initiateIdentityVerificationRequest);
        requestWrapper.setRequestTime(requestTime.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN)));

        when(identityVerificationService.initiateIdentityVerification(any(),any())).thenReturn(response);

        mockMvc.perform(post("/identity-verification/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWrapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists());
    }

    @Test
    public void initiateIdentityVerification_throwsException_thenFail() throws Exception {
        InitiateIdentityVerificationRequest initiateIdentityVerificationRequest=new InitiateIdentityVerificationRequest();
        initiateIdentityVerificationRequest.setState("state");
        initiateIdentityVerificationRequest.setAuthorizationCode("authcode");
        RequestWrapper<InitiateIdentityVerificationRequest> requestWrapper = new RequestWrapper<>();
        ZonedDateTime requestTime = ZonedDateTime.now(ZoneOffset.UTC);
        requestWrapper.setRequest(initiateIdentityVerificationRequest);
        requestWrapper.setRequestTime(requestTime.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN)));

        when(identityVerificationService.initiateIdentityVerification(any(), any())).thenThrow(new SignUpException("Exception Occurred"));

        mockMvc.perform(post("/identity-verification/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWrapper)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    public void getIdentityVerifierDetails_withValidDetails_thenPass() throws Exception {
        String transactionId = "transactionId";
        String id = "id";
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode mockResponse = objectMapper.createObjectNode();
        mockResponse.put("key", "value");

        when(identityVerificationService.getIdentityVerifierDetails(transactionId, id)).thenReturn(mockResponse);

        mockMvc.perform(get("/identity-verification/identity-verifier/{id}", id)
                        .cookie(new Cookie(SignUpConstants.IDV_TRANSACTION_ID, transactionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists());

    }

    @Test
    public void getSlot_withValidDetails_thenPass() throws Exception {
        SlotResponse mockResponse = new SlotResponse();
        mockResponse.setSlotId("slotId");
        String transactionId = "transactionId";
        SlotRequest slotRequest=new SlotRequest();
        slotRequest.setDisabilityType("VISION");
        slotRequest.setConsent("AGREE");
        slotRequest.setVerifierId("id");

        RequestWrapper<SlotRequest> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequest(slotRequest);
        ZonedDateTime requestTime = ZonedDateTime.now(ZoneOffset.UTC);
        requestWrapper.setRequestTime(requestTime.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN)));

        when(identityVerificationService.getSlot(Mockito.anyString(), any(), any())).thenReturn(mockResponse);

        mockMvc.perform(post("/identity-verification/slot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWrapper))
                        .cookie(new Cookie(SignUpConstants.IDV_TRANSACTION_ID, transactionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists());
    }

    @Test
    public void getStatus_withValidDetails_thenPass() throws Exception {
        IdentityVerificationStatusResponse mockResponse = new IdentityVerificationStatusResponse();
        String transactionId = "transactionId";

        when(identityVerificationService.getStatus(transactionId)).thenReturn(mockResponse);

        mockMvc.perform(get("/identity-verification/status")
                        .cookie(new Cookie(SignUpConstants.IDV_SLOT_ALLOTTED, transactionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists());
    }

    @Test
    public void getStatus_throwsException_thenFail() throws Exception {
        String transactionId = "transactionId";
        when(identityVerificationService.getStatus(transactionId)).thenThrow(new SignUpException("Exception Occurred"));
        mockMvc.perform(get("/identity-verification/status")
                        .cookie(new Cookie(SignUpConstants.IDV_SLOT_ALLOTTED, transactionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

}
