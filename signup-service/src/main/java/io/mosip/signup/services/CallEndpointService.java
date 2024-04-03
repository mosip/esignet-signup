package io.mosip.signup.services;


import io.mosip.signup.dto.*;
import io.mosip.signup.exception.SignUpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Service
public class CallEndpointService {

    @Autowired
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate selfTokenRestTemplate;

    public CallEndpointService(RestTemplate selfTokenRestTemplate) {
        this.selfTokenRestTemplate = selfTokenRestTemplate;
    }

    public <T extends Object> RestResponseWrapper<T> callEndpoint(
            RestRequestWrapper<?> restRequest,
            HttpMethod httpMethodType, String endPoint, String exceptionMessage, String applicationId) {
        try {
            RestResponseWrapper<T> restResponseWrapper;
            restResponseWrapper = selfTokenRestTemplate.exchange(
                    endPoint,
                    httpMethodType,
                    new HttpEntity<>(restRequest),
                    new ParameterizedTypeReference<RestResponseWrapper<T>>() {
                    }, applicationId
            ).getBody();
            return restResponseWrapper;
        } catch (RestClientException restClientException) {
            log.error("Endpoint {} is unreachable.", endPoint);
            throw new SignUpException(exceptionMessage);
        }
    }
}
