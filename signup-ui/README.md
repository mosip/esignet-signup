# Signup UI

## Overview

This repository contains the source code for the MOSIP Signup Services UI.
It provides functionality to verify a user’s phone number via OTP. Upon successful verification, the user can register in the integrated ID registry. Both registration and password reset processes require OTP verification. Additionally, the system supports KYC verification.

Signup UI contains the following pages:

1. Registration
2. Reset Password
3. Identity Verification

- **/signup**:
    This page prompts the user to enter a mobile number for registration. After the number is provided, it is verified by sending an OTP. Once the verification is successful, a signup form is displayed, allowing the user to complete their registration.

- **/reset-password**:
    This page displays a form where you need to enter your mobile number and full name. After submission, your identity will be verified through an OTP. Once verified, two fields—New Password and Confirm Password—will appear, allowing you to set and reset your password.

- **/identity-verification**: 
    This page cannot be accessed directly; the user must initiate their transaction through eSignet. When a relying party requests verified claims, the user is redirected to the signup identity-verification page before completing authentication in eSignet to perform KYC.

    On this page, the user is guided through the steps, starting with selecting the type of KYC. Additional instructions are provided, especially for video KYC verification. The process requires camera permission, after which the video verification begins. The user must follow the on-screen instructions.

    Once the verification is complete, the result will be either successful or failed. In both cases, the user is redirected back to the eSignet page.

## Signup Dynamic Form
Starting from version **1.3.0**, the registration form becomes fully dynamic and configurable.
It is defined using a UI-spec JSON file, which is accessed through an API. To render this UI-spec JSON, an independent library called [json-form-builder](https://github.com/mosip/mosip-sdk/blob/master/json-form-builder/README.md) has been developed and integrated here.
For details on how this library is used within signup-ui, refer to [dynamic-form](./../docs/design/dynamic-forms.md).

## Local Development

### First Setup

To install relevant npm packages, run the following script:

```bash
npm install
```

### Running Locally

#### Browser

During development, it is recommended to use Chrome, and it needed to be open with --disable-web-security mode to avoid CORS errors.

Mac users use the following command:

```bash
open -na Google\ Chrome --args --user-data-dir=/tmp/temporary-chrome-profile-dir --disable-web-security
```

Ubuntu users use the following command:

```bash
google-chrome-stable --user-data-dir="~/ dev session" --disable-web-security
```

#### Scripts

##### Web app

To start the web application, run the following script:

```bash
npm start
```

This opens [http://localhost:3000](http://localhost:3000) to view it in the browser.

##### Storybook

To start the storybook, run the following script:

```bash
npm storybook
```

This opens [http://localhost:6006](http://localhost:6006) in the browser. Making change to the components in the project triggers hot reload in storybook.

##### Testing

To run all test cases, run the following command:

```bash
npm test
```

#### Environments

`.env.example` file is provided in the root folder. In the development, copy and paste the variables described into a self-created `.env.local` file, replacing the required values accordingly.

By default, the environment variables are:
| variable | value |
| -------- | ----- |
| `REACT_APP_API_BASE_URL` | http://localhost:8088/v1/signup |

#### Translation

New translations can be added in the `locales` folder of the `public` folder. Also, newly added key(s) need to be added to `resource.d.ts` to fulfill the type and get the key suggestion.

## NPM Highlights

- `react-webcam`: It allows to easily capture video and still images directly from the user's webcam, offering a range of options for customization and control.
- `socket.io-client`: It allows bi-directional communication between client and server.
- `zustand`: A small, fast and scalable bear bones state-management solution using simplified flux principles. Has a comfy API based on hooks, isn't boilerplate or opinionated.
- `@mosip/json-form-builder`: A custom form builder library build by mosip, to generate a form with validation by passing ui-schema.
