package io.mosip.signup.dto;

import io.mosip.signup.util.RegistrationStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Data
public class RegistrationTransaction implements Serializable {

    private String challengeHash;
    private String identifier;
    private LocalDateTime startedAt;
    private int challengeRetryAttempts;
    private LocalDateTime lastRetryAt;
    private String challengeTransactionId;
    private String applicationId;
    private RegistrationStatus registrationStatus;
    private String locale;

    public RegistrationTransaction(String identifier) {
        this.identifier = identifier;
        this.startedAt = LocalDateTime.now(ZoneOffset.UTC);
        this.challengeTransactionId = UUID.randomUUID().toString();
        this.applicationId = UUID.randomUUID().toString();
        this.registrationStatus = null;
        this.challengeHash = null;
        this.challengeRetryAttempts = 0;
        this.lastRetryAt = null;
    }

    public long getLastRetryToNow() {
        if (this.lastRetryAt == null) return 0;

        return this.lastRetryAt.until(LocalDateTime.now(ZoneOffset.UTC), ChronoUnit.SECONDS);
    }

    public void increaseAttempt() {
        this.challengeRetryAttempts += 1;
        this.lastRetryAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
