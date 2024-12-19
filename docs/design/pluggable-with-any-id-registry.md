## Define ID registry plugin

Tight integration of signup service with MOSIP ID-repository should be removed. Follow the same eSignet plugin approach to integrate
signup service with any ID registry systems.

In signup service version < 1.1.0, Integration was used to create user identity entry and update user identity. Validation of the input user 
identity data was carried out in signup service before posting the request to MOSIP ID repository. ID repository also validated the identity
data in the request using MOSIP defined Identity schema.

Once the data is validated and saved in ID repo, the same was published to MOSIP IDA. Only after the identity data is published to IDA, end user
will be able to authenticate via eSignet using the created identity. So it is required for signup service to check the status of the identity in
ID registry before confirming registration completion to the end user.

**Note:** eSignet connects to MOSIP IDA via 'Authenticator' plugin.

Refer : [ProfileRegistryPlugin.java](../../signup-integration-api/src/main/java/io/mosip/signup/api/spi/ProfileRegistryPlugin.java)