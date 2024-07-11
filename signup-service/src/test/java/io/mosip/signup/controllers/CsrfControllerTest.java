package io.mosip.signup.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(value = CsrfController.class,  excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class CsrfControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RestTemplate restTemplate;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getCsrfToken_withValidToken_returnSuccessResponse() throws Exception, JsonProcessingException {
        CsrfToken csrfToken = new DefaultCsrfToken("headerName", "parameterName", "token");
        mockMvc.perform(get("/csrf/token").content(objectMapper.writeValueAsString(csrfToken))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }
}
