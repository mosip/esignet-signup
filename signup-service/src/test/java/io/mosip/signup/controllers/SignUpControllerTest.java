package io.mosip.signup.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.signup.helper.AuditHelper;
import io.mosip.signup.services.RegistrationService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @MockBean
    RestTemplate restTemplate;

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

}
