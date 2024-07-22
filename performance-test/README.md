
### Contains
* This folder contains performance test script of below API endpoint categories.
    1. Creating KBA Challenge value for Reset Password usecase (Setup)
    2. S01 User SignUp (Execution)
    3. S02 User Reset Password (Execution)
	4. S03 UserSignUpRegistrationStatus (Execution)
	5. S04 ResetPasswordStatus (Execution)
	6. tearDown Thread Group (Execution)

* Open source Tools used,
    1. [Apache JMeter](https://jmeter.apache.org/)

### How to run performance scripts using Apache JMeter tool
* Download Apache JMeter from https://jmeter.apache.org/download_jmeter.cgi
* Download scripts for the required module.
* Start JMeter by running the jmeter.bat file for Windows or jmeter file for Unix. 
* Validate the scripts for one user.
* Execute a dry run for 10 min.
* Execute performance run with various loads in order to achieve targeted NFR's.

### Setup points for Execution

* We need some jar files which needs to be added in lib folder of jmeter, PFA dependency links for your reference : 

   * bcprov-jdk15on-1.66.jar
      * <!-- https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk15on -->
			<dependency>
				<groupId>org.bouncycastle</groupId>
				<artifactId>bcprov-jdk15on</artifactId>
				<version>1.66</version>
			</dependency>

   * jjwt-api-0.11.2.jar
      * <!-- https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api -->
			<dependency>
				<groupId>io.jsonwebtoken</groupId>
				<artifactId>jjwt-api</artifactId>
				<version>0.11.2</version>
			</dependency>

   * jjwt-impl-0.11.2.jar
       * <!-- https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-impl -->
			<dependency>
				<groupId>io.jsonwebtoken</groupId>
				<artifactId>jjwt-impl</artifactId>
				<version>0.11.2</version>
				<scope>runtime</scope>
			</dependency>

   * jjwt-jackson-0.11.2.jar
       * <!-- https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-jackson -->
			<dependency>
				<groupId>io.jsonwebtoken</groupId>
				<artifactId>jjwt-jackson</artifactId>
				<version>0.11.2</version>
				<scope>runtime</scope>
			</dependency>

   * nimbus-jose-jwt-9.25.6.jar  
       * <!-- https://mvnrepository.com/artifact/com.nimbusds/nimbus-jose-jwt -->
			<dependency>
				<groupId>com.nimbusds</groupId>
				<artifactId>nimbus-jose-jwt</artifactId>
				<version>9.25.6</version>
			</dependency>

	* jmeter-plugins-synthesis-2.2.jar
		* <!-- https://jmeter-plugins.org/files/packages/jpgc-synthesis-2.2.zip -->

### Downloading Plugin manager jar file for the purpose installing other JMeter specific plugins

* Download JMeter plugin manager from below url links.
	*https://jmeter-plugins.org/get/

* After downloading the jar file place it in below folder path.
	*lib/ext

* Please refer to following link to download JMeter jars.
	https://mosip.atlassian.net/wiki/spaces/PT/pages/1227751491/Steps+to+set+up+the+local+system#PluginManager

### Execution points for eSignet Sign Up Services API's

*SignUp_Test_Script.jmx
	
	* Creating KBA Challenge value for Reset Password usecase (Setup) : This thread contains 3 API's i.e. generate challenge, verify challenge and register API endpoints. Will be generating base64url-encoded json value of the khmer name details with the json body. We will be saving the encoded value with the khmer name in csv file;.
	
	*S01 User SignUp (Execution):
		*S01 T01 GetCsrf: This thread generated CSRF token
		*S01 T02 GenerateChallenge: This thread contains Generate Challenge endpoint API. We need to pass an identifier value which is nothing but a 8-10 digit phone number with country code as the prefix. We are using a preprocessor from which we are getting the random generated phone number.
		*S01 T03 VerifyChallenge: This thread contains verify challenge API in which we will pass the value of identifier i.e. phone number and transaction id in the headers which will get from the csv file generated in preparation. The file generated can't be used for multiple iterations. We need to increase the expiry time of the transaction id we are getting from the preparation thread group so for that we need to update the mentioned property mosip.signup.unauthenticated.txn.timeout in signup default properties.
		*S01 T04 Register: This thread is for register API endpoint and will use a csv file to pass the value of identifier and verified transaction id. Will use the file generated from the preparation and it can't be used multiple times. We need to increase the expiry time of the transaction id we are getting from the preparation thread group so for that we need to update the mentioned property mosip.signup.register.txn.timeout in signup default properties.
		*S01 T05 Registration: This thread contains Registration Status API endpoint. Will use the file generated from the preparation to pass the transaction id and it can be used multiple times as it will only give the latest status for the transaction id we are passing. The transaction id used has a expiry time which can be configured with the mentioned property mosip.signup.status-check.txn.timeout available in mosip config signup default properties. We need to increase the expiry time of the transaction id we are getting from the preparation thread group so for that we need to update the mentioned property mosip.signup.status-check.txn.timeout in signup default properties.
		
	*S02 User Reset Password (Execution):
		*S02 T01 GenerateChallenge: This thread contains Generate Challenge endpoint API. We need to pass an identifier value which is nothing but a 8-10 digit phone number with country code as the prefix. We are using a preprocessor from which we are getting the random generated phone number.
		*S02 T02 VerifyChallenge: This thread contains verify challenge API in which we will pass the value of identifier i.e. phone number and transaction id in the headers which will get from the csv file generated in preparation. The file generated can't be used for multiple iterations. We need to increase the expiry time of the transaction id we are getting from the preparation thread group so for that we need to update the mentioned property mosip.signup.unauthenticated.txn.timeout in signup default properties.
		*S02 T03 ResetPassword: This thread contains Reset Password API endpoint. Will use the file generated from the preparation to pass the base64url-encoded json value in the verify challenge. The transaction id generated by verify challenge will passed in reset-password endpoint url.
		*S02 T04 Registration: This thread contains Registration status API endpoint. This API checks whether we have successfully reset the password.
		
	*S03 UserSignUpRegistrationStatus (Execution):
		*S03 RegistrationCheckStatus: This thread verifies whether registration has successfully moved to COMPLETED state.
		
	*S04 ResetPasswordStatus (Execution):
		*S04 ResetPasswordCheckStatus: This thread verifies whether reset password has successfully moved to COMPLETED state.
	
	*tearDown Thread Group (Execution):
		* This thread group clears all property values generated during execution.
		
	
### Designing the workload model for performance test execution
* Calculation of number of users depending on Transactions per second (TPS) provided by client

* Applying little's law
	* Users = TPS * (SLA of transaction + think time + pacing)
	* TPS --> Transaction per second.
	
* For the realistic approach we can keep (Think time + Pacing) = 1 second for API testing
	* Calculating number of users for 10 TPS
		* Users= 10 X (SLA of transaction + 1)
		       = 10 X (1 + 1)
			   = 20
			   
### Usage of Constant Throughput timer to control Hits/sec from JMeter
* In order to control hits/ minute in JMeter, it is better to use Timer called Constant Throughput Timer.

* If we are performing load test with 10TPS as hits / sec in one thread group. Then we need to provide value hits / minute as in Constant Throughput Timer
	* Value = 10 X 60
			= 600

* Dropdown option in Constant Throughput Timer
	* Calculate Throughput based on as = All active threads in current thread group
		* If we are performing load test with 10TPS as hits / sec in one thread group. Then we need to provide value hits / minute as in Constant Throughput Timer
	 			Value = 10 X 60
					  = 600
		  
	* Calculate Throughput based on as = this thread
		* If we are performing scalability testing we need to calculate throughput for 10 TPS as 
          Value = (10 * 60 )/(Number of users)
