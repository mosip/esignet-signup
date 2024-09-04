/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.signup.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.esignet.core.dto.RequestWrapper;
import io.mosip.signup.api.util.ProfileCreateUpdateStatus;
import io.mosip.signup.dto.RegistrationStatusResponse;
import io.mosip.signup.dto.ResetPasswordRequest;
import io.mosip.signup.helper.AuditHelper;
import io.mosip.signup.services.RegistrationService;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.util.SignUpConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import static io.mosip.esignet.core.constants.Constants.UTC_DATETIME_PATTERN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@WebMvcTest(value = ResetPasswordController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ActiveProfiles(value = {"test"})
public class ResetPasswordControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RestTemplate restTemplate;

    @MockBean
    RegistrationService registrationService;

    @MockBean
    AuditHelper auditHelper;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void restPassword_thenPass() throws Exception {
        String mockTransactionID = "123456789";
        RequestWrapper<ResetPasswordRequest> resetPasswordWrapper = new RequestWrapper<>();
        ZonedDateTime requestTime = ZonedDateTime.now(ZoneOffset.UTC);

        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setIdentifier("+855123456789");
        resetPasswordRequest.setPassword("Password@2023");

        resetPasswordWrapper.setRequestTime(requestTime.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN)));
        resetPasswordWrapper.setRequest(resetPasswordRequest);

        RegistrationStatusResponse registrationStatusResponse = new RegistrationStatusResponse();
        registrationStatusResponse.setStatus(ProfileCreateUpdateStatus.PENDING);

        when(registrationService.updatePassword(any(), any())).thenReturn(registrationStatusResponse);

        mockMvc.perform(post("/reset-password")
                        .content(objectMapper.writeValueAsString(resetPasswordWrapper))
                        .cookie(new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.status").value("PENDING"));
    }

    @Test
    public void resetPassword_withBlankPassword_returnInvalidPassword() throws Exception{
        String mockTransactionID = "123456789";
        RequestWrapper<ResetPasswordRequest> resetPasswordWrapper = new RequestWrapper<>();
        ZonedDateTime requestTime = ZonedDateTime.now(ZoneOffset.UTC);

        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setIdentifier("+855123456789");
        resetPasswordRequest.setPassword("");

        resetPasswordWrapper.setRequestTime(requestTime.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN)));
        resetPasswordWrapper.setRequest(resetPasswordRequest);

        mockMvc.perform(post("/reset-password")
                        .content(objectMapper.writeValueAsString(resetPasswordWrapper))
                        .cookie(new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_PASSWORD));
    }

    @Test
    public void resetPassword_withNullPassword_returnInvalidPassword() throws Exception{
        String mockTransactionID = "123456789";
        RequestWrapper<ResetPasswordRequest> resetPasswordWrapper = new RequestWrapper<>();
        ZonedDateTime requestTime = ZonedDateTime.now(ZoneOffset.UTC);

        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setIdentifier("+855123456789");
        resetPasswordRequest.setPassword(null);

        resetPasswordWrapper.setRequestTime(requestTime.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN)));
        resetPasswordWrapper.setRequest(resetPasswordRequest);

        mockMvc.perform(post("/reset-password")
                        .content(objectMapper.writeValueAsString(resetPasswordWrapper))
                        .cookie(new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_PASSWORD));
    }

    @Test
    public void resetPassword_withPasswordInvalidPattern_returnInvalidPassword() throws Exception{
        String mockTransactionID = "123456789";
        RequestWrapper<ResetPasswordRequest> resetPasswordWrapper = new RequestWrapper<>();
        ZonedDateTime requestTime = ZonedDateTime.now(ZoneOffset.UTC);

        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setIdentifier("+855123456789");
        resetPasswordRequest.setPassword("123456789");

        resetPasswordWrapper.setRequestTime(requestTime.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN)));
        resetPasswordWrapper.setRequest(resetPasswordRequest);

        RegistrationStatusResponse registrationStatusResponse = new RegistrationStatusResponse();
        registrationStatusResponse.setStatus(ProfileCreateUpdateStatus.PENDING);

        when(registrationService.updatePassword(any(), any())).thenReturn(registrationStatusResponse);

        mockMvc.perform(post("/reset-password")
                        .content(objectMapper.writeValueAsString(resetPasswordWrapper))
                        .cookie(new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_PASSWORD));
    }

    @Test
    public void resetPassword_withPasswordMoreThenAndLeastThenLength_returnInvalidPassword() throws Exception{
        String mockTransactionID = "123456789";
        RequestWrapper<ResetPasswordRequest> resetPasswordWrapper = new RequestWrapper<>();
        ZonedDateTime requestTime = ZonedDateTime.now(ZoneOffset.UTC);

        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setIdentifier("+855123456789");
        resetPasswordRequest.setPassword("12345");

        resetPasswordWrapper.setRequestTime(requestTime.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN)));
        resetPasswordWrapper.setRequest(resetPasswordRequest);

        RegistrationStatusResponse registrationStatusResponse = new RegistrationStatusResponse();
        registrationStatusResponse.setStatus(ProfileCreateUpdateStatus.PENDING);

        when(registrationService.updatePassword(any(), any())).thenReturn(registrationStatusResponse);

        mockMvc.perform(post("/reset-password")
                        .content(objectMapper.writeValueAsString(resetPasswordWrapper))
                        .cookie(new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_PASSWORD));

        resetPasswordRequest.setPassword("123451234512345123451234512345");
        mockMvc.perform(post("/reset-password")
                        .content(objectMapper.writeValueAsString(resetPasswordWrapper))
                        .cookie(new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_PASSWORD));
    }

    @Test
    public void resetPassword_withIdentifierInvalidPattern_returnInvalidPassword() throws Exception{
        String mockTransactionID = "123456789";
        RequestWrapper<ResetPasswordRequest> resetPasswordWrapper = new RequestWrapper<>();
        ZonedDateTime requestTime = ZonedDateTime.now(ZoneOffset.UTC);

        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setIdentifier("+855123");
        resetPasswordRequest.setPassword("Password@2023");

        resetPasswordWrapper.setRequestTime(requestTime.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN)));
        resetPasswordWrapper.setRequest(resetPasswordRequest);

        RegistrationStatusResponse registrationStatusResponse = new RegistrationStatusResponse();
        registrationStatusResponse.setStatus(ProfileCreateUpdateStatus.PENDING);

        when(registrationService.updatePassword(any(), any())).thenReturn(registrationStatusResponse);

        mockMvc.perform(post("/reset-password")
                        .content(objectMapper.writeValueAsString(resetPasswordWrapper))
                        .cookie(new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_IDENTIFIER));
    }

    @Test
    public void resetPassword_withBlankIdentifier_returnInvalidPassword() throws Exception{
        String mockTransactionID = "123456789";
        RequestWrapper<ResetPasswordRequest> resetPasswordWrapper = new RequestWrapper<>();
        ZonedDateTime requestTime = ZonedDateTime.now(ZoneOffset.UTC);

        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setIdentifier("");
        resetPasswordRequest.setPassword("Password@2023");

        resetPasswordWrapper.setRequestTime(requestTime.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN)));
        resetPasswordWrapper.setRequest(resetPasswordRequest);

        RegistrationStatusResponse registrationStatusResponse = new RegistrationStatusResponse();
        registrationStatusResponse.setStatus(ProfileCreateUpdateStatus.PENDING);

        when(registrationService.updatePassword(any(), any())).thenReturn(registrationStatusResponse);

        mockMvc.perform(post("/reset-password")
                        .content(objectMapper.writeValueAsString(resetPasswordWrapper))
                        .cookie(new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_IDENTIFIER));
    }

    @Test
    public void resetPassword_withNullIdentifier_returnInvalidPassword() throws Exception{
        String mockTransactionID = "123456789";
        RequestWrapper<ResetPasswordRequest> resetPasswordWrapper = new RequestWrapper<>();
        ZonedDateTime requestTime = ZonedDateTime.now(ZoneOffset.UTC);

        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setIdentifier(null);
        resetPasswordRequest.setPassword("Password@2023");

        resetPasswordWrapper.setRequestTime(requestTime.format(DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN)));
        resetPasswordWrapper.setRequest(resetPasswordRequest);

        RegistrationStatusResponse registrationStatusResponse = new RegistrationStatusResponse();
        registrationStatusResponse.setStatus(ProfileCreateUpdateStatus.PENDING);

        when(registrationService.updatePassword(any(), any())).thenReturn(registrationStatusResponse);

        mockMvc.perform(post("/reset-password")
                        .content(objectMapper.writeValueAsString(resetPasswordWrapper))
                        .cookie(new Cookie(SignUpConstants.TRANSACTION_ID, mockTransactionID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorConstants.INVALID_IDENTIFIER));
    }
}
