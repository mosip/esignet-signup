package io.mosip.signup.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiChallengeRequest implements Serializable {
    private String key;
}
