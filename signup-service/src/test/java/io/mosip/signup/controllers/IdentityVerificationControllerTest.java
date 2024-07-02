package io.mosip.signup.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.esignet.core.dto.RequestWrapper;
import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.dto.*;
import io.mosip.signup.exception.SignUpException;
import io.mosip.signup.helper.AuditHelper;
import io.mosip.signup.services.IdentityVerificationService;
import io.mosip.signup.util.ErrorConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import javax.servlet.http.HttpServletResponse;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = IdentityVerificationController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ActiveProfiles(value = {"test"})
public class IdentityVerificationControllerTest {

    @MockBean
    IdentityVerificationService identityVerificationService;

    @MockBean
    AuditHelper auditHelper;


    @Autowired
    MockMvc mockMvc;

    @Mock
    private HttpServletResponse httpServletResponse;

    ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void initiateIdentityVerification_withValidDetails_thenPass() throws Exception {

        InitiateIdentityVerificationRequest initiateIdentityVerificationRequest = new InitiateIdentityVerificationRequest();
        initiateIdentityVerificationRequest.setState("state");
        initiateIdentityVerificationRequest.setAuthorizationCode("authorizationCode");

        RequestWrapper requestWrapper = new RequestWrapper();
        requestWrapper.setRequest(initiateIdentityVerificationRequest);
        requestWrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());

        InitiateIdentityVerificationResponse initiateIdentityVerificationResponse = new InitiateIdentityVerificationResponse();
        IdentityVerifierDetail identityVerifierDetail = new IdentityVerifierDetail();
        identityVerifierDetail.setId("verifierId");
        IdentityVerifierDetail [] identityVerifierDetails = {identityVerifierDetail};
        initiateIdentityVerificationResponse.setIdentityVerifiers(identityVerifierDetails);
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        when(identityVerificationService.initiateIdentityVerification(Mockito.any(), Mockito.any()))
                .thenReturn(initiateIdentityVerificationResponse);




        mockMvc.perform(post("/identity-verification/initiate")
                        .content(objectMapper.writeValueAsString(requestWrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.identityVerifiers[0].id").value("verifierId"));
    }


    @Test
    public void initiateIdentityVerification_withInValidDetails_thenFail() throws Exception {

        InitiateIdentityVerificationRequest initiateIdentityVerificationRequest = new InitiateIdentityVerificationRequest();
        initiateIdentityVerificationRequest.setState("state");
        initiateIdentityVerificationRequest.setAuthorizationCode("authorizationCode");

        RequestWrapper requestWrapper = new RequestWrapper();
        requestWrapper.setRequest(initiateIdentityVerificationRequest);
        requestWrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());

        when(identityVerificationService.initiateIdentityVerification(Mockito.any(), Mockito.any()))
                .thenThrow(new SignUpException(ErrorConstants.GRANT_EXCHANGE_FAILED));

        mockMvc.perform(post("/identity-verification/initiate")
                        .content(objectMapper.writeValueAsString(requestWrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.GRANT_EXCHANGE_FAILED));
    }

    @Test
    public void getIdentityVerifierDetails_withValidDetails_thenPass() throws Exception {

        when(identityVerificationService.getIdentityVerifierDetails(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(objectMapper.readTree("{\"id\":\"verifierId\"}"));

        mockMvc.perform(get("/identity-verification/identity-verifier/id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    public void getIdentityVerifierDetails_withInValidDetails_thenPass() throws Exception {

        when(identityVerificationService.getIdentityVerifierDetails(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new SignUpException(ErrorConstants.INVALID_IDENTITY_VERIFIER_ID));

        mockMvc.perform(get("/identity-verification/identity-verifier/id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_IDENTITY_VERIFIER_ID));
    }

    @Test
    public void getSlot_withValidDetails_thenPass() throws Exception {

        RequestWrapper requestWrapper = new RequestWrapper();
        SlotRequest slotRequest= new SlotRequest();
        slotRequest.setVerifierId("verifierId");
        requestWrapper.setRequest(slotRequest);
        requestWrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());

        SlotResponse slotResponse = new SlotResponse();
        slotResponse.setSlotId("slotId");
        when(identityVerificationService.getSlot(Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(slotResponse);

        mockMvc.perform(post("/identity-verification/slot")
                        .content(objectMapper.writeValueAsString(requestWrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.response.slotId").value("slotId"));
    }


    @Test
    public void getSlot_withInValidDetails_thenPass() throws Exception {

        RequestWrapper requestWrapper = new RequestWrapper();
        SlotRequest slotRequest= new SlotRequest();
        slotRequest.setVerifierId("verifierId");
        requestWrapper.setRequest(slotRequest);
        requestWrapper.setRequestTime(IdentityProviderUtil.getUTCDateTime());

        SlotResponse slotResponse = new SlotResponse();
        slotResponse.setSlotId("slotId");
        when(identityVerificationService.getSlot(Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenThrow(new SignUpException(ErrorConstants.SLOT_NOT_AVAILABLE));

        mockMvc.perform(post("/identity-verification/slot")
                        .content(objectMapper.writeValueAsString(requestWrapper))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.SLOT_NOT_AVAILABLE));
    }
}

