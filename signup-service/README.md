## SignUp Service

Signup service is a spring boot application with endpoints to

1. Generate Challenge (Only OTP supported)
2. Verify Challenge
3. Register user with verified transaction
4. Check registration status
5. Reset the password of the registered user

Signup service connects to MOSIP IDRepo Identity service to register the verified user as an identity record.
ID Repo identity service publishes the registered identity to MOSIP IDA. This enables authentication with the registered
username and password with eSignet.

Publishing registered/updated identity to MOSIP IDA is an async process. Hence, status endpoint is configured to check
the latest status from server after every configured interval from signup UI.

### Signup service uses spring cache to store the transaction details.

Registration flow:

| Endpoint          | Cache                                                                | Evict                                               |
|-------------------|----------------------------------------------------------------------|-----------------------------------------------------|
| generateChallenge | challenge-generated (k: transactionId, v: SignupTransaction)         |                                                     |
| verifyChallenge   | challenge-verified (k: verified-transactionId, v: SignupTransaction)       | challenge-generated (k: transactionId, v: SignupTransaction)       |
| register          | status-check (k: verified-transactionId, v: SignupTransaction) | challenge-verified (k: verified-transactionId, v: SignupTransaction) |
| status            | status-check (k: verified-transactionId, v: SignupTransaction) |     |

Reset Password flow:

| Endpoint          | Cache                                                                      | Evict                                               |
|-------------------|----------------------------------------------------------------------------|-----------------------------------------------------|
| generateChallenge | challenge-generated (k: transactionId, v: SignupTransaction)         |                                                     |
| verifyChallenge   | challenge-verified (k: verified-transactionId, v: SignupTransaction) | challenge-generated (k: transactionId, v: SignupTransaction)       |
| resetPassword     | status-check (k: verified-transactionId, v: SignupTransaction)           | challenge-verified (k: verified-transactionId, v: SignupTransaction) |
| status            | status-check (k: verified-transactionId, v: SignupTransaction)       |     |


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

   3.2 Download kernel-auth-adapter-1.2.1-es-SNAPSHOT.jar file from [here](https://oss.sonatype.org/#nexus-search;gav~io.mosip.kernel~kernel-auth-adapter~~~~kw,versionexpand).

   3.3 Add that file to "signup-service" in Project Structure settings of IntelliJ, and Apply.

   3.4 right click on file signup-service/src/main/java/io/mosip/signup/SignUpServiceApplication.java and click on Run

