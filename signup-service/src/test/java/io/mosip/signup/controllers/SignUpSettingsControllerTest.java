package io.mosip.signup.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@RunWith(SpringRunner.class)
@WebMvcTest(value = SignUpSettingsController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ActiveProfiles(value = {"test"})
public class SignUpSettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void getSignupSettings_thenPass () throws Exception {
        mockMvc.perform(get("/settings")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"))
                .andExpect(jsonPath("$.responseTime").isNotEmpty())
                .andExpect(jsonPath("$.response.configs").isNotEmpty())
                .andExpect(jsonPath("$['response']['configs']['identifier.pattern']").value("\\+855\\d{8,9}"))
                .andExpect(jsonPath("$['response']['configs']['identifier.prefix']").value("+855"))
                .andExpect(jsonPath("$['response']['configs']['captcha.site.key']").value("6LcdIvsoAAAAAMq"))
                .andExpect(jsonPath("$['response']['configs']['otp.length']").value(6))
                .andExpect(jsonPath("$['response']['configs']['password.pattern']").value("^.{8,}$"))
                .andExpect(jsonPath("$['response']['configs']['challenge.timeout']").value(60))
                .andExpect(jsonPath("$['response']['configs']['resend.attempts']").value(3))
                .andExpect(jsonPath("$['response']['configs']['resend.delay']").value(60))
                .andExpect(jsonPath("$['response']['configs']['fullname.pattern']").value(".*"))
                .andExpect(jsonPath("$['response']['configs']['status.request.delay']").value(20))
                .andExpect(jsonPath("$['response']['configs']['status.request.limit']").value(10))
                .andExpect(jsonPath("$.errors").isEmpty());
    }
}
