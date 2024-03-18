package io.mosip.signup.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.esignet.core.dto.RequestWrapper;
import io.mosip.signup.dto.RegistrationStatusResponse;
import io.mosip.signup.dto.ResetPasswordRequest;
import io.mosip.signup.helper.AuditHelper;
import io.mosip.signup.services.RegistrationService;
import io.mosip.signup.util.ActionStatus;
import io.mosip.signup.util.ErrorConstants;
import io.mosip.signup.util.RegistrationStatus;
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

import javax.servlet.http.Cookie;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static io.mosip.esignet.core.constants.Constants.UTC_DATETIME_PATTERN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@RunWith(SpringRunner.class)
@WebMvcTest(value = SignUpController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ActiveProfiles(value = {"test"})
public class SignUpControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RegistrationService registrationService;

    @MockBean
    AuditHelper auditHelper;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void getSignupSettings_thenPass () throws Exception {
        mockMvc.perform(get("/settings")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.responseTime").isNotEmpty())
                .andExpect(jsonPath("$.response.configs").isNotEmpty())
                .andExpect(jsonPath("$['response']['configs']['identifier.pattern']").value("^\\+855[1-9]\\d{7,8}$"))
                .andExpect(jsonPath("$['response']['configs']['identifier.prefix']").value("+855"))
                .andExpect(jsonPath("$['response']['configs']['captcha.site.key']").value("6LcdIvsoAAAAAMq"))
                .andExpect(jsonPath("$['response']['configs']['otp.length']").value(6))
                .andExpect(jsonPath("$['response']['configs']['password.pattern']").value("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\x5F\\W])(?=.{8,20})[a-zA-Z0-9\\x5F\\W]{8,20}$"))                .andExpect(jsonPath("$['response']['configs']['challenge.timeout']").value(60))
                .andExpect(jsonPath("$['response']['configs']['resend.attempts']").value(3))
                .andExpect(jsonPath("$['response']['configs']['resend.delay']").value(60))
                .andExpect(jsonPath("$['response']['configs']['fullname.pattern']").value("^[\\u1780-\\u17FF\\u19E0-\\u19FF\\u1A00-\\u1A9F\\u0020]{1,30}$"))
                .andExpect(jsonPath("$['response']['configs']['status.request.delay']").value(20))
                .andExpect(jsonPath("$['response']['configs']['status.request.limit']").value(10))
                .andExpect(jsonPath("$['response']['configs']['signin.redirect-url']").value("https://esignet.dev.mosip.net/authorize"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

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
        registrationStatusResponse.setStatus(RegistrationStatus.PENDING);

        when(registrationService.updatePassword(any(), any())).thenReturn(registrationStatusResponse);

        mockMvc.perform(post("/reset-password")
                        .header("locale", "khm")
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
        registrationStatusResponse.setStatus(RegistrationStatus.PENDING);

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
        registrationStatusResponse.setStatus(RegistrationStatus.PENDING);

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
        registrationStatusResponse.setStatus(RegistrationStatus.PENDING);

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
        registrationStatusResponse.setStatus(RegistrationStatus.PENDING);

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
        registrationStatusResponse.setStatus(RegistrationStatus.PENDING);

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
