package io.mosip.signup.dto;

import io.mosip.esignet.core.util.IdentityProviderUtil;
import io.mosip.signup.util.Purpose;
import io.mosip.signup.util.RegistrationStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Data
public class RegistrationTransaction implements Serializable {

    private String challengeHash;
    private String identifier;
    private LocalDateTime startedAt;
    private int verificationAttempts;
    private int challengeRetryAttempts;
    private LocalDateTime lastRetryAt;
    private String challengeTransactionId;
    private String applicationId;
    private Map<String, RegistrationStatus> handlesStatus;
    private RegistrationStatus registrationStatus;
    private String locale;
    private Purpose purpose;
    private String uin;

    public RegistrationTransaction(String identifier, Purpose purpose) {
        this.identifier = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                identifier.toLowerCase(Locale.ROOT));
        this.startedAt = LocalDateTime.now(ZoneOffset.UTC);
        this.challengeTransactionId = UUID.randomUUID().toString();
        this.applicationId = UUID.randomUUID().toString();
        this.handlesStatus = new HashMap<>();
        this.registrationStatus = null;
        this.challengeHash = null;
        this.challengeRetryAttempts = 0;
        this.verificationAttempts = 0;
        this.lastRetryAt = null;
        this.purpose = purpose;
    }

    public long getLastRetryToNow() {
        if (this.lastRetryAt == null) return 0;

        return this.lastRetryAt.until(LocalDateTime.now(ZoneOffset.UTC), ChronoUnit.SECONDS);
    }

    public void increaseAttempt() {
        this.challengeRetryAttempts += 1;
        this.lastRetryAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public void increaseVerificationAttempt() {
        this.verificationAttempts += 1;
        this.lastRetryAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public boolean isValidIdentifier(String inputIdentifier) {
        return this.identifier.equals(IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256,
                inputIdentifier));
    }

    public void setIdentifier(String identifier) {
        this.identifier = IdentityProviderUtil.generateB64EncodedHash(IdentityProviderUtil.ALGO_SHA3_256, identifier);
    }
}
