## SignUp Service

Signup service is a spring boot application with endpoints to

1. Generate Challenge (Only OTP supported)
2. Verify Challenge
3. Register user with verified transaction
4. Check registration status
5. Reset the password of the registered user
6. Websocket handler and endpoints to support video identity verification process.

In this version, signup-service require below MOSIP kernel modules and its dependents:
1. kernel otpmanager
2. kernel auditmanager
3. kernel authmanager
4. kernel notifier

**Note:** To run signup-service locally `mosip.internal.domain.url` property should be set with a valid base URL of any MOSIP(LTS) environment.

### Signup service uses spring cache to store the transaction details.

#### Registration flow:

| Endpoint          | Cache                                                                | Evict                                                                |
|-------------------|----------------------------------------------------------------------|----------------------------------------------------------------------|
| generateChallenge | challenge_generated (k: transactionId, v: SignupTransaction)         |                                                                      |
| verifyChallenge   | challenge_verified (k: verified-transactionId, v: SignupTransaction) | challenge_generated (k: transactionId, v: SignupTransaction)         |
| register          | status_check (k: verified-transactionId, v: SignupTransaction)       | challenge_verified (k: verified-transactionId, v: SignupTransaction) |
| status            | status_check (k: verified-transactionId, v: SignupTransaction)       |                                                                      |

#### Reset Password flow:

| Endpoint          | Cache                                                                | Evict                                                                |
|-------------------|----------------------------------------------------------------------|----------------------------------------------------------------------|
| generateChallenge | challenge_generated (k: transactionId, v: SignupTransaction)         |                                                                      |
| verifyChallenge   | challenge_verified (k: verified-transactionId, v: SignupTransaction) | challenge_generated (k: transactionId, v: SignupTransaction)         |
| resetPassword     | status_check (k: verified-transactionId, v: SignupTransaction)       | challenge_verified (k: verified-transactionId, v: SignupTransaction) |
| status            | status_check (k: verified-transactionId, v: SignupTransaction)       |                                                                      |


#### Identity Verification flow:

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


## Build & Run (for developers)
The project requires JDK 11.
1. Build and install:
    ```
    $ mvn clean install -Dgpg.skip=true -Dmaven.gitcommitid.skip=true
    ```
2. Run with IntelliJ IDE

   3.1 Right click on signup-service and click on "Open module settings".

   3.2 Click on "+" to add jars from external directory.

   3.3 Choose below jars under signup-service/target/signup-plugins and click on "Apply" and "Ok" button.

      -> [kernel-auth-adapter-lite.jar](target/signup-plugins/kernel-auth-adapter-lite.jar)

      -> [esignet-mock-plugin.jar](target/signup-plugins/esignet-mock-plugin.jar)
   
   3.4 Update below properties in [application-local.properties](src/main/resources/application-local.properties) to point to right MOSIP environment.

      -> `mosip.internal.domain.url=https://api-internal.dev.mosip.net`

      -> `keycloak.external.url=https://iam.dev.mosip.net`

      -> `mosip.signup.client.secret=actual-secret`

   3.4 Go to [SignUpServiceApplication.java](src/main/java/io/mosip/signup/SignUpServiceApplication.java) and run from the main class.

