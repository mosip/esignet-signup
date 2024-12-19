# Signup UI

## Overview

Signup UI has provision to verify the user's phone number with OTP and on successful verification user is allowed to 
register in the integrated ID registry.Both register and reset password requires OTP verification.

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
* `react-webcam`: It allows to easily capture video and still images directly from the user's webcam, offering a range of options for customization and control.
* `socket.io-client`: It allows bi-directional communication between client and server.
* `zustand`: A small, fast and scalable bear bones state-management solution using simplified flux principles. Has a comfy API based on hooks, isn't  boilerplate or opinionated.