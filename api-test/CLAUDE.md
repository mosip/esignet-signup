# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

API test rig for **eSignet Signup** service — part of the MOSIP identity platform. Tests the signup, challenge/verify, password-reset, L2 identity-verification, and OIDC/OAuth flows using TestNG + REST Assured with YAML-driven test definitions.

- **Artifact**: `apitest-esignet-signup` v1.4.0-SNAPSHOT
- **Main class**: `io.mosip.testrig.apirig.signup.testrunner.MosipTestRunner`
- **Single dependency**: `io.mosip.testrig.apitest.commons:apitest-commons:1.5.0`

## Commands

### Build
```powershell
mvn clean install -Dgpg.skip=true -Dmaven.gitcommitid.skip=true
```

### Run via JAR (primary)
```powershell
java -Dmodules=signup -Denv.user=<envName> -Denv.endpoint=<baseUrl> -Denv.testLevel=smokeAndRegression `
  -jar target/apitest-esignet-signup-1.4.0-SNAPSHOT-jar-with-dependencies.jar
```
`testLevel` options: `smoke`, `regression`, `smokeAndRegression`

### Run via Maven
```powershell
mvn clean install -Dgpg.skip=true -Dmaven.gitcommitid.skip=true -Dmodules=signup -Denv.user=<envName>
```

### Rebuild shared commons (only when apitest-commons changes)
```powershell
cd mosip-functional-tests/apitest-commons
mvn clean install -Dgpg.skip=true -Dmaven.gitcommitid.skip=true
```

## Architecture

### TestNG Suite Hierarchy
```
signupMasterTestSuite.xml
├── signupPrerequisiteSuite.xml   ← runs first; sets up PMS policy/partner/OIDC client
└── signupSuite.xml               ← main test cases
```

`signupPrerequisiteSuite.xml` tests are tagged `<parameter name="prerequisite" value="Yes"/>` and run unconditionally to prepare test data (identity records, policy groups, API keys, OIDC clients).

`signupSuite.xml` covers:
- Core flows: `SignUpSettings`, `GenerateChallenge`, `VerifyChallenge`, `RegisterUser`, `GetRegistrationStatus`, `ResetPassword`
- Password-auth OIDC flow: `OAuthDetailsRequestPassword` → `AuthenticateUserPassword` → `AuthorizationCodePassword` → `GenerateTokenPassword` → `GetOidcUserInfoPassword`
- L2 identity-verification flow: `OAuthDetailsRequestV3` → `AuthenticateUserV3` → `ClaimDetails` → `PrepareSignupRedirect` → `SignupAuthorize` → `IDTAuthentication` → `SignupAuthorizeCode` → `InitiateIdVerification` → `GetIdVerifier` → `GetSlot` → `WebSocketConnection` → `GetIdentityVerificationStatus` → `CompleteSignupVerification` → `AuthorizationCode` → `GenerateToken` → `GetOidcUserInfo`
- `RegistrationFlowL2/` — same L2 flow but for already-registered users (mirrored sub-folder)
- Negative test cases (`*NegTC`) for each positive flow

### Two Execution Modes (MosipTestRunner)

The runner auto-detects the identity plugin at startup:
- **Mock-Identity-System**: OTPs are pre-configured; skips biometric device/key generation steps.
- **MOSIP-ID**: Full live instance; generates Keycloak users, biometric test data, RSA keys.

### Java Source Layout
```
src/main/java/io/mosip/testrig/apirig/signup/
├── testrunner/MosipTestRunner.java     ← entry point; environment init
├── testscripts/                        ← one class per HTTP interaction pattern
│   ├── SimplePost                      ← POST with body, validates JSON response
│   ├── GetWithParam                    ← GET (polls for status endpoints)
│   ├── SimplePostForAutoGenId          ← POST; extracts + stores response field(s) as IDs
│   ├── SimplePostForAutoGenIdForUrlEncoded  ← same for application/x-www-form-urlencoded
│   ├── PostWithAutogenIdWithOtpGenerate ← POST that triggers OTP generation mid-flow
│   ├── PostWithOnlyPathParam            ← POST with only path params (no body)
│   ├── PatchWithPathParamsAndBody       ← PATCH with path params + body
│   ├── PutWithPathParamsAndBody         ← PUT with path params + body
│   ├── AddIdentity                      ← prerequisite: creates identity records
│   └── WebSocketConnection             ← WebSocket interaction
└── utils/
    ├── SignupUtil.java      ← core utility; extends parent AdminTestUtil; keyword replacement, request helpers
    ├── SignupConfigManager  ← loads signup.properties; exposes typed config accessors
    ├── SignupConstants      ← OTP/boolean string constants
    └── SignupCustomWebSocketClientUtil
```

All test script classes extend `SignupUtil` and follow the same pattern: `@DataProvider` reads the YAML file named by the `ymlFile` TestNG parameter, then `@Test` iterates over test cases, applies keyword replacement, fires the request, and validates JSON output.

### Test Definition Pattern (YAML + HBS)

Each test folder under `src/main/resources/signup/<TestName>/` contains:
- **`<TestName>.yml`** — test case definitions
- **`<camelCase>.hbs`** — Handlebars input/output templates (optional; used when body structure varies)

YAML structure:
```yaml
<TopLevelKey>:
   <TestCaseName>:
      endPoint: /v1/signup/...
      uniqueIdentifier: TC_ESignet_..._01
      role: resident           # controls which auth token is used
      restMethod: get|post|put|patch
      inputTemplate: signup/<folder>/<inputHbs>    # path without .hbs
      outputTemplate: signup/<folder>/<outputHbs>  # path without .hbs
      input: '{ "field": "value" }'
      output: '{ "status": "SUCCESS" }'
```

Optional fields: `description`, `checkStatusCodeOnlyInResponse: true`, `idKeyName` (for auto-id extraction), `testLevel: smoke|regression`.

### Keyword Replacement in Input/Output

Special tokens replaced at runtime by `SignupUtil`:

| Token | Replaced with |
|-------|--------------|
| `$TIMESTAMP$` | Current ISO-8601 timestamp |
| `$PHONENUMBERFROMREGEXFORSIGNUP$` | Random phone number matching the country regex |
| `$FULLNAMETOREGISTERUSER$` | Locale-appropriate full name |
| `$PASSWORDTOREGISTERUSER$` | Value of `PASSWORD_FOR_ADDIDENTITY_AND_REGISTRATION` |
| `$PASSWORDTORESET$` | Value of `PASSWORD_TO_RESET` |
| `$1STLANG$` / `$2NDLANG$` | First/second supported language codes |
| `$ID:<testCaseName>_<fieldName>$` | Value stored by a previous test (cross-test dependency) |

### Cross-Test ID Dependencies

`SimplePostForAutoGenId` extracts the `idKeyName` field(s) from the response and writes them to a shared map keyed by `<testCaseName>_<fieldName>`. Subsequent tests reference these values with `$ID:<testCaseName>_<fieldName>$`.

Example: `OAuthDetailsRequestPassword` extracts `transactionId`; `AuthenticateUserPassword` references it as `$ID:OAuthDetailsRequestPassword_All_Valid_Smoke_sid_transactionId$`.

### Key Configuration (`src/main/resources/config/signup.properties`)

| Property | Purpose |
|----------|---------|
| `eSignetbaseurl` | Base URL for eSignet service |
| `signupBaseUrl` | Base URL for signup service |
| `keycloak-external-url` | Keycloak IAM URL |
| `mosip_components_base_urls` | Semicolon-separated `component=host` overrides |
| `mockNotificationChannel` | `email`, `phone`, or `email,phone` |
| `uinGenerationProcessingDelayTimeInMilliSeconds` | Wait after UIN generation (default 90000) |
| `uinGenMaxLoopCount` | OTP retry limit (default 20) |
| `testCasesToExecute` | Comma-separated test case names to run (empty = run all) |
| `generateDependencyJson` | Set `yes` to regenerate `testCaseInterDependency_*.json` (local IDE only) |

### URL Routing Logic (SimplePost)

- Endpoints containing `/signup/` use `SignupConfigManager.getSignupBaseUrl()`
- Test names prefixed `ESignet_` use `SignupConfigManager.getEsignetBaseUrl()`
- All others use `ApplnURI` (the `env.endpoint` JVM argument)
