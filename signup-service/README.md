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
1. Build Docker for a service:
    ```
    $ docker build -f Dockerfile
    ```