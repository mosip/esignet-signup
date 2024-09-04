## SignUp Service

Signup service is a spring boot application with endpoints to

1. Generate Challenge (Only OTP supported)
2. Verify Challenge
3. Register user with verified transaction
4. Check registration status
5. Reset the password of the registered user
6. Websocket handler and endpoints to support video identity verification process.

Signup service connects to MOSIP IDRepo Identity service to register the verified user as an identity record.
ID Repo identity service publishes the registered identity to MOSIP IDA. This enables authentication with the registered
username and password with eSignet.

Publishing registered/updated identity to MOSIP IDA is an async process. Hence, status endpoint is configured to check
the latest status from server after every configured interval from signup UI.

### Signup service uses spring cache to store the transaction details.

Registration flow:

| Endpoint          | Cache                                                                | Evict                                                                |
|-------------------|----------------------------------------------------------------------|----------------------------------------------------------------------|
| generateChallenge | challenge_generated (k: transactionId, v: SignupTransaction)         |                                                                      |
| verifyChallenge   | challenge_verified (k: verified-transactionId, v: SignupTransaction) | challenge_generated (k: transactionId, v: SignupTransaction)         |
| register          | status_check (k: verified-transactionId, v: SignupTransaction)       | challenge_verified (k: verified-transactionId, v: SignupTransaction) |
| status            | status_check (k: verified-transactionId, v: SignupTransaction)       |                                                                      |

Reset Password flow:

| Endpoint          | Cache                                                                | Evict                                                                |
|-------------------|----------------------------------------------------------------------|----------------------------------------------------------------------|
| generateChallenge | challenge_generated (k: transactionId, v: SignupTransaction)         |                                                                      |
| verifyChallenge   | challenge_verified (k: verified-transactionId, v: SignupTransaction) | challenge_generated (k: transactionId, v: SignupTransaction)         |
| resetPassword     | status_check (k: verified-transactionId, v: SignupTransaction)       | challenge_verified (k: verified-transactionId, v: SignupTransaction) |
| status            | status_check (k: verified-transactionId, v: SignupTransaction)       |                                                                      |


Identity Verification flow:

| Endpoint                        | Cache                                                                    | Evict                                                                                                                                                                        |
|---------------------------------|--------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| identity-verification           | identity_verification (k: transactionId, v: IdentityVerificationTransaction) |                                                                                                                                                                              |
| slot                            | slot_allotted (k: transactionId, v: IdentityVerificationTransaction)     | identity_verification (k: transactionId, v: IdentityVerificationTransaction)                                                                                                 |
| /ws                             | verififed_slot (k: slotId, v: IdentityVerificationTransaction)           | slot_allotted (k: transactionId, v: IdentityVerificationTransaction)                                                                                                         |
| process-frame                   | verififed_slot (k: slotId, v: IdentityVerificationTransaction)           |                                                                                                                                                                              |
| /topic/slotId (STOMP - publish) | verified_slot (k: slotId, v: IdentityVerificationTransaction)            |                                                                                                                                                                              |
| Event - WS connect              | slots_connected(connection-id)                                   |                                                                                                                                                                              |
| Event - WS disconnect           |                                                                          | slot_allotted (k: transactionId, v: IdentityVerificationTransaction)  <br/>verififed_slot (k: slotId, v: IdentityVerificationTransaction) <br/>slots_connected(connection-id) |


> Note: slot_connected is a HSET in redis
> 
> slot_connected "connection-id-1" "connected-time-epoch-1" "connection-id-2" "connected-time-epoch-2"
> 
> On every scheduled interval `mosip.signup.slot.cleanup-cron` we run LUA script to remove expired connection-ids from slot_connected HSET cache.
> 
> TTL for each connection-id in the slot_connected HSET is defined -> `mosip.signup.slot.expire-in-seconds`
> 
> To get the current count of slots connected, we execute HLEN command on slot_connected cache.
> 
> Note: The connection-id is concatenation of transactionId and slotId with a separator.


## Build & run (for developers)
The project requires JDK 11.
1. Build and install:
    ```
    $ mvn clean install -Dgpg.skip=true
    ```
2. Build Docker for a service:
    ```
    $ docker build -f Dockerfile
    ```
   3. Run with IntelliJ IDEA

      3.1 Right click on parent POM file (pom.xml) and click on button "Add as Maven Project".

      3.2 Add below dependency in the signup-service pom.xml

   ```
            <dependency>
               <groupId>io.mosip.kernel</groupId>
               <artifactId>kernel-auth-adapter-lite</artifactId>
               <version>1.2.0.1-B4</version>
           </dependency>
   ```

      3.3 Add that file to "signup-service" in Project Structure settings of IntelliJ, and Apply.

      3.4 Open signup-service/src/main/java/io/mosip/signup/SignUpServiceApplication.java and click on Run

