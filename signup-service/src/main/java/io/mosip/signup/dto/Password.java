package io.mosip.signup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Password {

    private String hash;
    private String salt;

    @Data
    @AllArgsConstructor
    public static class PasswordPlaintext{
        private String inputData;
    }

    @Data
    public static class PasswordHash {
        private String hashValue;
        private String salt;
    }
}
