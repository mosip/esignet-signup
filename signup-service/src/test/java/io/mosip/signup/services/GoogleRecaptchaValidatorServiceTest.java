package io.mosip.signup.services;

import io.mosip.signup.dto.ReCaptchaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


class GoogleRecaptchaValidatorServiceTest {

    @InjectMocks
    GoogleRecaptchaValidatorService googleRecaptchaValidatorService;

    @Mock
    RestTemplate restTemplate;

    private String captchaVerifyUrl = "mockCaptchaVerifyUrl";
    private String verifierSecret = "mockVerifierSecret";


    @BeforeEach
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(googleRecaptchaValidatorService,
                "requiredCaptcha",
                true);
        ReflectionTestUtils.setField(googleRecaptchaValidatorService,
                "captchaVerifyUrl",
                captchaVerifyUrl);
        ReflectionTestUtils.setField(googleRecaptchaValidatorService,
                "verifierSecret",
                verifierSecret);
    }

    @Test
    void validateCaptcha_withNotRequiredCaptcha_thenTrue() {
        ReflectionTestUtils.setField(googleRecaptchaValidatorService,
                "requiredCaptcha",
                false);
        assertEquals(true, googleRecaptchaValidatorService.validateCaptcha("mockToken"));
    }

    @Test
    void validateCaptcha_withRecaptchaValidatorSuccess_thenTrue() {

        ReCaptchaResponse reCaptchaResponse = new ReCaptchaResponse();
        reCaptchaResponse.setSuccess(true);

        when(restTemplate.postForObject(eq(captchaVerifyUrl), any(), any())).thenReturn(reCaptchaResponse);
        assertEquals(true, googleRecaptchaValidatorService.validateCaptcha("mockToken"));
    }

    @Test
    void validateCaptcha_withRecaptchaValidatorFailure_thenFalse() {

        ReCaptchaResponse reCaptchaResponse = new ReCaptchaResponse();
        reCaptchaResponse.setSuccess(false);

        when(restTemplate.postForObject(eq(captchaVerifyUrl), any(), any())).thenReturn(reCaptchaResponse);
        assertEquals(false, googleRecaptchaValidatorService.validateCaptcha("mockToken"));
    }

    @Test
    void validateCaptcha_withRecaptchaValidatorNull_thenFalse() {
        assertEquals(false, googleRecaptchaValidatorService.validateCaptcha("mockToken"));
    }
}
