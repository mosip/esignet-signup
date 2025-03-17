
### Overview
* This folder contains performance test script of below API endpoint categories.
    1. Creating KBI Challenge Value For Reset Password Usecase (Setup)
    2. S01 User SignUp (Execution)
    3. S02 User Reset Password (Execution)
	4. S03 Status Call From Idrepo To IDA (Results)


* Open source Tools used,
    1. [Apache JMeter](https://jmeter.apache.org/)

### How to run performance scripts using Apache JMeter tool
* Download Apache JMeter from https://jmeter.apache.org/download_jmeter.cgi
* Download scripts for the required module.
* Start JMeter by running the jmeter.bat file for Windows or jmeter.sh file for Unix. 
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
	
* Please make sure the postgres JDBC jar file in lib/ folder of JMeter
	* postgresql-42.7.5.jar
	
### Schema update for the Password authentication.

* We have created new schema with version 0.4 in cellbox1 environment to support user registration with password where default schema does not password authentication. The identity schema is provided under the path 
	*performance-test/support-files/identity_schema.txt
	
### Execution points for eSignet Sign Up Services API's

*signup_test_script.jmx
	
	* Creating KBI Challenge value for Reset Password usecase (Setup) : This thread contains 3 API's i.e. generate challenge, verify challenge and register API endpoints. Will be generating base64url-encoded json value with the name details with the json body. We will be saving the encoded value along with the name in csv file.
	
	*S01 User SignUp (Execution):
		*S01 T01 Get Csrf Token: This API endpoint generats CSRF token. It contains JSR223 Pre-Processor to capture start time of test to access credentials data from idrepo database.
		*S01 T02 Generate Challenge: This API endpoint generates challenge for user registration. We need to pass an identifier value which is nothing but a 9-10 digit phone number with country code as the prefix.
		*S01 T03 Verify Challenge: This API endpoint performs verify challenge, which we will pass the value of identifier i.e. phone number.
		*S01 T04 Register: This API endpoint performs SignUp Registration for the user.
		*S01 T05 Registration Status: The API endpoint verifies the status of the registration.
		
	*S02 User Reset Password (Execution):
	    *S02 T01 Get Csrf Token: This API endpoint generats CSRF token. It contains JSR223 Pre-Processor to capture start time of test to access credentials data from idrepo database.
		*S02 T02 Generate Challenge: This API endpoint generates challenge for user reset password. We need to pass an identifier value which is nothing but a 9-10 digit phone number with country code as the prefix.
		*S02 T03 Verify Challenge: This API endpoint performs verify challenge API, which we will pass the value of identifier i.e. phone number  and encoded KBI value.
		*S02 T04 Reset Password: This API endpoint performs Reset Password operation. We will be generating new password from the JSR223- Preprocessor and passing it in request body.
		*S02 T05 Registration: This thread contains Registration status API endpoint. This API checks whether we have successfully reset the password.
		
	*S03 Status Call From Idrepo To IDA (Results):
		*We will be fetching the request_id, cr_dtimes from idrepo.credential_request_status table by passing start and end time in the query. The query provides list of request Ids. These request Ids will be passed in credential_transaction table of mosip_credential DB to fetch upd_dtiimes value. In JSR223 post-processor of the "Fetch Updated Time From Credential Table Query" , we compute response time of asynchronous call of request id by subtracting upd_dtimes with cr_dtimes. The final value as well as status will be passed in transaction controller S03 T01 ${status} Status Call.
			
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
